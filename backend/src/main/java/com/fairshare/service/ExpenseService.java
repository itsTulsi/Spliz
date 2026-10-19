package com.fairshare.service;

import com.fairshare.dto.CreateExpenseRequest;
import com.fairshare.dto.ExpenseResponseDto;
import com.fairshare.dto.ShareResponseDto;
import com.fairshare.entity.ExpenseEntity;
import com.fairshare.entity.ExpenseShareEntity;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.enums.SplitMethod;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.ExpenseRepository;
import com.fairshare.repository.ExpenseShareRepository;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitCalculationService splitCalculationService;
    private final ActivityLogService activityLogService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseShareRepository expenseShareRepository,
                          GroupRepository groupRepository,
                          GroupMemberRepository groupMemberRepository,
                          SplitCalculationService splitCalculationService,
                          ActivityLogService activityLogService) {
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.splitCalculationService = splitCalculationService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getGroupExpenses(Long groupId) {
        return expenseRepository.findByGroupIdOrderByOccurredAtDesc(groupId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long expenseId) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));
        return mapToDto(expense);
    }

    @Transactional
    public ExpenseResponseDto createExpense(Long groupId, CreateExpenseRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMemberEntity payer = groupMemberRepository.findByIdAndGroupId(request.getPaidByMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Payer does not belong to the target group."));

        // Calculate and validate shares according to method
        Map<Long, Long> shareMap = computeShares(request, groupId);

        ExpenseEntity expense = new ExpenseEntity(
            group,
            request.getDescription().trim(),
            request.getAmountPaise(),
            payer,
            request.getSplitMethod(),
            request.getOccurredAt()
        );

        ExpenseEntity savedExpense = expenseRepository.save(expense);

        for (Map.Entry<Long, Long> entry : shareMap.entrySet()) {
            GroupMemberEntity shareMember = groupMemberRepository.findByIdAndGroupId(entry.getKey(), groupId)
                    .orElseThrow(() -> new BusinessException("Participant member ID " + entry.getKey() + " does not belong to group"));
            ExpenseShareEntity share = new ExpenseShareEntity(savedExpense, shareMember, entry.getValue(), null);
            savedExpense.getShares().add(share);
            expenseShareRepository.save(share);
        }

        activityLogService.logActivity(
            group,
            "EXPENSE",
            savedExpense.getId(),
            ActivityEventType.EXPENSE_CREATED,
            String.format("Expense '%s' of ₹%.2f recorded by %s.",
                savedExpense.getDescription(),
                savedExpense.getAmountPaise() / 100.0,
                payer.getDisplayName())
        );

        return mapToDto(savedExpense);
    }

    @Transactional
    public ExpenseResponseDto updateExpense(Long expenseId, CreateExpenseRequest request) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        Long groupId = expense.getGroup().getId();
        GroupMemberEntity payer = groupMemberRepository.findByIdAndGroupId(request.getPaidByMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Payer does not belong to the target group."));

        Map<Long, Long> shareMap = computeShares(request, groupId);

        expense.setDescription(request.getDescription().trim());
        expense.setAmountPaise(request.getAmountPaise());
        expense.setPaidByMember(payer);
        expense.setSplitMethod(request.getSplitMethod());
        if (request.getOccurredAt() != null) {
            expense.setOccurredAt(request.getOccurredAt());
        }

        expenseShareRepository.deleteByExpenseId(expenseId);
        expense.getShares().clear();

        for (Map.Entry<Long, Long> entry : shareMap.entrySet()) {
            GroupMemberEntity shareMember = groupMemberRepository.findByIdAndGroupId(entry.getKey(), groupId)
                    .orElseThrow(() -> new BusinessException("Participant does not belong to group: " + entry.getKey()));
            ExpenseShareEntity share = new ExpenseShareEntity(expense, shareMember, entry.getValue(), null);
            expense.getShares().add(share);
            expenseShareRepository.save(share);
        }

        ExpenseEntity saved = expenseRepository.save(expense);

        activityLogService.logActivity(
            saved.getGroup(),
            "EXPENSE",
            saved.getId(),
            ActivityEventType.EXPENSE_UPDATED,
            String.format("Expense '%s' updated (₹%.2f, %s).", saved.getDescription(), saved.getAmountPaise() / 100.0, saved.getSplitMethod())
        );

        return mapToDto(saved);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        GroupEntity group = expense.getGroup();
        String desc = expense.getDescription();
        long amount = expense.getAmountPaise();

        expenseRepository.delete(expense);

        activityLogService.logActivity(
            group,
            "EXPENSE",
            expenseId,
            ActivityEventType.EXPENSE_DELETED,
            String.format("Expense '%s' of ₹%.2f deleted. Running balances recalculated.", desc, amount / 100.0)
        );
    }

    private Map<Long, Long> computeShares(CreateExpenseRequest request, Long groupId) {
        SplitMethod method = request.getSplitMethod() != null ? request.getSplitMethod() : SplitMethod.EQUAL;
        long totalPaise = request.getAmountPaise();

        if (method == SplitMethod.EQUAL) {
            List<Long> participants = request.getParticipantMemberIds();
            if (participants == null || participants.isEmpty()) {
                // If not supplied, default to all current members in the group
                participants = groupMemberRepository.findByGroupIdOrderByIdAsc(groupId).stream()
                        .map(GroupMemberEntity::getId)
                        .collect(Collectors.toList());
            }
            return splitCalculationService.calculateEqualSplit(totalPaise, participants);
        } else if (method == SplitMethod.EXACT) {
            return splitCalculationService.validateAndCalculateExactSplit(totalPaise, request.getShares());
        } else if (method == SplitMethod.PERCENTAGE) {
            return splitCalculationService.calculatePercentageSplit(totalPaise, request.getShares());
        }

        throw new BusinessException("Unsupported split method: " + method);
    }

    private ExpenseResponseDto mapToDto(ExpenseEntity e) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(e.getId());
        dto.setGroupId(e.getGroup().getId());
        dto.setDescription(e.getDescription());
        dto.setAmountPaise(e.getAmountPaise());
        dto.setPaidByMemberId(e.getPaidByMember().getId());
        dto.setPaidByMemberName(e.getPaidByMember().getDisplayName());
        dto.setSplitMethod(e.getSplitMethod());
        dto.setOccurredAt(e.getOccurredAt());
        dto.setCreatedAt(e.getCreatedAt());

        List<ShareResponseDto> shareDtos = new ArrayList<>();
        if (e.getShares() != null) {
            for (ExpenseShareEntity s : e.getShares()) {
                shareDtos.add(new ShareResponseDto(
                    s.getMember().getId(),
                    s.getMember().getDisplayName(),
                    s.getSharePaise(),
                    s.getPercentageBasisPoints()
                ));
            }
        }
        dto.setShares(shareDtos);
        return dto;
    }
}
