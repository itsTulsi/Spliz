package com.fairshare.service;

import com.fairshare.dto.PreviewSplitRequest;
import com.fairshare.dto.PreviewSplitResponse;
import com.fairshare.dto.ShareInputDto;
import com.fairshare.dto.ShareResponseDto;
import com.fairshare.entity.ExpenseEntity;
import com.fairshare.entity.ExpenseShareEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.enums.SplitMethod;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.ExpenseRepository;
import com.fairshare.repository.ExpenseShareRepository;
import com.fairshare.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FairnessService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitCalculationService splitCalculationService;
    private final ActivityLogService activityLogService;

    public FairnessService(ExpenseRepository expenseRepository,
                           ExpenseShareRepository expenseShareRepository,
                           GroupMemberRepository groupMemberRepository,
                           SplitCalculationService splitCalculationService,
                           ActivityLogService activityLogService) {
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.splitCalculationService = splitCalculationService;
        this.activityLogService = activityLogService;
    }

    /**
     * Non-mutating preview comparing current split against a proposed split rule.
     */
    @Transactional(readOnly = true)
    public PreviewSplitResponse previewSplit(Long expenseId, PreviewSplitRequest request) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        Long groupId = expense.getGroup().getId();
        List<GroupMemberEntity> groupMembers = groupMemberRepository.findByGroupIdOrderByIdAsc(groupId);
        Map<Long, String> memberNameMap = new HashMap<>();
        for (GroupMemberEntity m : groupMembers) {
            memberNameMap.put(m.getId(), m.getDisplayName());
        }

        // 1. Current shares from persistent storage
        List<ShareResponseDto> currentShares = new ArrayList<>();
        for (ExpenseShareEntity s : expense.getShares()) {
            currentShares.add(new ShareResponseDto(
                s.getMember().getId(),
                memberNameMap.getOrDefault(s.getMember().getId(), "Member #" + s.getMember().getId()),
                s.getSharePaise(),
                s.getPercentageBasisPoints()
            ));
        }

        // 2. Compute proposed shares using authoritative calculation service
        long totalPaise = request.getAmountPaise() != null ? request.getAmountPaise() : expense.getAmountPaise();
        SplitMethod method = request.getSplitMethod() != null ? request.getSplitMethod() : SplitMethod.EQUAL;

        Map<Long, Long> proposedMap;
        if (method == SplitMethod.EQUAL) {
            proposedMap = splitCalculationService.calculateEqualSplit(totalPaise, request.getParticipantMemberIds());
        } else if (method == SplitMethod.EXACT) {
            proposedMap = splitCalculationService.validateAndCalculateExactSplit(totalPaise, request.getShares());
        } else {
            proposedMap = splitCalculationService.calculatePercentageSplit(totalPaise, request.getShares());
        }

        List<ShareResponseDto> proposedShares = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : proposedMap.entrySet()) {
            proposedShares.add(new ShareResponseDto(
                entry.getKey(),
                memberNameMap.getOrDefault(entry.getKey(), "Member #" + entry.getKey()),
                entry.getValue(),
                null
            ));
        }

        PreviewSplitResponse response = new PreviewSplitResponse();
        response.setExpenseId(expenseId);
        response.setCurrency("INR");
        response.setSplitMethod(method);
        response.setTotalAmountPaise(totalPaise);
        response.setCurrentShares(currentShares);
        response.setProposedShares(proposedShares);
        response.setPersisted(false);

        return response;
    }

    /**
     * Persist confirmed proposed split scenario transactionally.
     */
    @Transactional
    public void applySplit(Long expenseId, PreviewSplitRequest request) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        long totalPaise = request.getAmountPaise() != null ? request.getAmountPaise() : expense.getAmountPaise();
        SplitMethod method = request.getSplitMethod() != null ? request.getSplitMethod() : SplitMethod.EQUAL;

        Map<Long, Long> allocatedShares;
        if (method == SplitMethod.EQUAL) {
            allocatedShares = splitCalculationService.calculateEqualSplit(totalPaise, request.getParticipantMemberIds());
        } else if (method == SplitMethod.EXACT) {
            allocatedShares = splitCalculationService.validateAndCalculateExactSplit(totalPaise, request.getShares());
        } else {
            allocatedShares = splitCalculationService.calculatePercentageSplit(totalPaise, request.getShares());
        }

        Map<Long, Integer> percentageMap = new HashMap<>();
        if (request.getShares() != null) {
            for (ShareInputDto s : request.getShares()) {
                if (s.getMemberId() != null && s.getPercentageBasisPoints() != null) {
                    percentageMap.put(s.getMemberId(), s.getPercentageBasisPoints());
                }
            }
        }

        expense.setSplitMethod(method);
        expense.setAmountPaise(totalPaise);

        // Map existing shares by member ID for in-place update
        Map<Long, ExpenseShareEntity> existingShareMap = expense.getShares().stream()
                .collect(Collectors.toMap(s -> s.getMember().getId(), s -> s));

        // Remove shares no longer participating (orphanRemoval issues SQL DELETE)
        expense.getShares().removeIf(s -> !allocatedShares.containsKey(s.getMember().getId()));

        // In-place update existing shares (issues SQL UPDATE) or add new shares (issues SQL INSERT)
        for (Map.Entry<Long, Long> entry : allocatedShares.entrySet()) {
            Long memberId = entry.getKey();
            Long sharePaise = entry.getValue();
            Integer pct = percentageMap.get(memberId);

            ExpenseShareEntity existing = existingShareMap.get(memberId);
            if (existing != null) {
                existing.setSharePaise(sharePaise);
                existing.setPercentageBasisPoints(pct);
            } else {
                GroupMemberEntity member = groupMemberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
                ExpenseShareEntity share = new ExpenseShareEntity(expense, member, sharePaise, pct);
                expense.getShares().add(share);
            }
        }

        expenseRepository.saveAndFlush(expense);

        // Audit log
        activityLogService.logActivity(
            expense.getGroup(),
            "EXPENSE",
            expense.getId(),
            ActivityEventType.SCENARIO_APPLIED,
            String.format("Fairness Simulator scenario applied to expense '%s'. Split method updated to %s.", expense.getDescription(), method)
        );
    }
}
