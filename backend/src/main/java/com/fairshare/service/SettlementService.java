package com.fairshare.service;

import com.fairshare.dto.CancelledExpenseDto;
import com.fairshare.dto.ConfirmSettlementPaymentRequest;
import com.fairshare.dto.InitiateUpiPaymentRequest;
import com.fairshare.dto.RecordSettlementRequest;
import com.fairshare.dto.SettlementResponseDto;
import com.fairshare.dto.SettleUpPreviewDto;
import com.fairshare.dto.SettleUpResultDto;
import com.fairshare.entity.ExpenseEntity;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.enums.SettlementStatus;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.ExpenseRepository;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import com.fairshare.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ActivityLogService activityLogService;
    private final UpiPaymentService upiPaymentService;
    private final ExpenseRepository expenseRepository;

    public SettlementService(SettlementRepository settlementRepository,
                             GroupRepository groupRepository,
                             GroupMemberRepository groupMemberRepository,
                             ActivityLogService activityLogService,
                             UpiPaymentService upiPaymentService,
                             ExpenseRepository expenseRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.activityLogService = activityLogService;
        this.upiPaymentService = upiPaymentService;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDto> getGroupSettlements(Long groupId) {
        return settlementRepository.findByGroupIdOrderByPaidAtDesc(groupId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlementById(Long groupId, Long settlementId) {
        SettlementEntity settlement = settlementRepository.findByIdAndGroupId(settlementId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));
        return mapToDto(settlement);
    }

    /**
     * Direct settlement recording (e.g. cash, bank transfer, manual log).
     */
    @Transactional
    public SettlementResponseDto recordSettlement(Long groupId, RecordSettlementRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        if (request.getFromMemberId().equals(request.getToMemberId())) {
            throw new BusinessException("Settlement payer and recipient must be different group members.");
        }

        GroupMemberEntity fromMember = groupMemberRepository.findByIdAndGroupId(request.getFromMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Payer does not belong to group: " + request.getFromMemberId()));

        GroupMemberEntity toMember = groupMemberRepository.findByIdAndGroupId(request.getToMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Recipient does not belong to group: " + request.getToMemberId()));

        SettlementEntity settlement = new SettlementEntity(
            group,
            fromMember,
            toMember,
            request.getAmountPaise(),
            request.getNote(),
            request.getPaidAt() != null ? request.getPaidAt() : Instant.now(),
            SettlementStatus.RECORDED,
            "MANUAL",
            null,
            UUID.randomUUID().toString()
        );

        SettlementEntity saved = settlementRepository.save(settlement);

        activityLogService.logActivity(
            group,
            "SETTLEMENT",
            saved.getId(),
            ActivityEventType.SETTLEMENT_RECORDED,
            String.format("Payment of ₹%.2f recorded: %s paid %s.",
                saved.getAmountPaise() / 100.0,
                fromMember.getDisplayName(),
                toMember.getDisplayName())
        );

        return mapToDto(saved);
    }

    /**
     * Step 1: Initiate UPI Payment Intent.
     * Generates a pending settlement attempt and compatible standard + app-specific UPI URIs.
     */
    @Transactional
    public SettlementResponseDto initiateUpiPayment(Long groupId, InitiateUpiPaymentRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        if (request.getFromMemberId().equals(request.getToMemberId())) {
            throw new BusinessException("Payer and recipient must be different group members.");
        }

        GroupMemberEntity fromMember = groupMemberRepository.findByIdAndGroupId(request.getFromMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Payer does not belong to group: " + request.getFromMemberId()));

        GroupMemberEntity toMember = groupMemberRepository.findByIdAndGroupId(request.getToMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Recipient does not belong to group: " + request.getToMemberId()));

        // Allow inline updating or configuration of recipient's UPI ID if supplied
        if (request.getRecipientUpiId() != null && !request.getRecipientUpiId().isBlank()) {
            upiPaymentService.validateUpiId(request.getRecipientUpiId());
            toMember.setUpiId(request.getRecipientUpiId().trim());
            groupMemberRepository.save(toMember);
        }

        if (toMember.getUpiId() == null || toMember.getUpiId().isBlank()) {
            throw new BusinessException("Recipient (" + toMember.getDisplayName() + ") does not have a UPI ID configured. Please configure their UPI ID to continue.");
        }
        upiPaymentService.validateUpiId(toMember.getUpiId());

        // Idempotency check: if an idempotency key was supplied and already initiated, return it
        String key = (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey().trim()
                : UUID.randomUUID().toString();

        Optional<SettlementEntity> existing = settlementRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            SettlementEntity existingEntity = existing.get();
            if (existingEntity.getGroup().getId().equals(groupId)) {
                return mapToDto(existingEntity);
            }
        }

        String app = request.getPaymentApp() != null ? request.getPaymentApp().trim().toUpperCase() : "GENERIC_UPI";
        String note = request.getNote() != null && !request.getNote().isBlank() ? request.getNote().trim() : "FairShare settlement";

        SettlementEntity settlement = new SettlementEntity(
            group,
            fromMember,
            toMember,
            request.getAmountPaise(),
            note,
            Instant.now(),
            SettlementStatus.INITIATED,
            "UPI",
            app,
            key
        );

        SettlementEntity saved = settlementRepository.save(settlement);
        return mapToDto(saved);
    }

    /**
     * Step 2: Confirm or update status when returning from payment app.
     */
    @Transactional
    public SettlementResponseDto confirmSettlementPayment(Long groupId, Long settlementId, ConfirmSettlementPaymentRequest request) {
        SettlementEntity settlement = settlementRepository.findByIdAndGroupId(settlementId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));

        // If already completed or verified, return idempotently
        if (settlement.getStatus() == SettlementStatus.REPORTED_SUCCESS ||
            settlement.getStatus() == SettlementStatus.VERIFIED ||
            settlement.getStatus() == SettlementStatus.RECORDED) {
            return mapToDto(settlement);
        }

        SettlementStatus newStatus = request.getStatus();

        if (newStatus == SettlementStatus.REPORTED_SUCCESS) {
            settlement.setStatus(SettlementStatus.REPORTED_SUCCESS);
            settlement.setPaidAt(Instant.now());
            if (request.getTransactionRef() != null && !request.getTransactionRef().isBlank()) {
                settlement.setTransactionRef(request.getTransactionRef().trim());
            }
            if (request.getNote() != null && !request.getNote().isBlank()) {
                settlement.setNote(request.getNote().trim());
            }

            activityLogService.logActivity(
                settlement.getGroup(),
                "SETTLEMENT",
                settlement.getId(),
                ActivityEventType.SETTLEMENT_RECORDED,
                String.format("Payment of ₹%.2f reported by %s to %s via UPI (%s). Self-reported settlement recorded.",
                    settlement.getAmountPaise() / 100.0,
                    settlement.getFromMember().getDisplayName(),
                    settlement.getToMember().getDisplayName(),
                    settlement.getUpiApp() != null ? settlement.getUpiApp() : "UPI")
            );
        } else if (newStatus == SettlementStatus.CANCELLED) {
            settlement.setStatus(SettlementStatus.CANCELLED);
        } else if (newStatus == SettlementStatus.FAILED) {
            settlement.setStatus(SettlementStatus.FAILED);
        } else if (newStatus == SettlementStatus.PENDING_CONFIRMATION) {
            settlement.setStatus(SettlementStatus.PENDING_CONFIRMATION);
        } else {
            throw new BusinessException("Unsupported settlement confirmation status: " + newStatus);
        }

        SettlementEntity saved = settlementRepository.save(settlement);
        return mapToDto(saved);
    }

    public SettlementResponseDto mapToDto(SettlementEntity s) {
        SettlementResponseDto dto = new SettlementResponseDto();
        dto.setId(s.getId());
        dto.setGroupId(s.getGroup().getId());
        dto.setFromMemberId(s.getFromMember().getId());
        dto.setFromMemberName(s.getFromMember().getDisplayName());
        dto.setToMemberId(s.getToMember().getId());
        dto.setToMemberName(s.getToMember().getDisplayName());
        dto.setToMemberUpiId(s.getToMember().getUpiId());
        dto.setAmountPaise(s.getAmountPaise());
        dto.setStatus(s.getStatus());
        dto.setPaymentMethod(s.getPaymentMethod());
        dto.setUpiApp(s.getUpiApp());
        dto.setIdempotencyKey(s.getIdempotencyKey());
        dto.setTransactionRef(s.getTransactionRef());
        dto.setNote(s.getNote());
        dto.setPaidAt(s.getPaidAt());
        dto.setCreatedAt(s.getCreatedAt());

        if (s.getToMember().getUpiId() != null && !s.getToMember().getUpiId().isBlank()) {
            try {
                String standardUri = upiPaymentService.buildStandardUpiUri(
                    s.getToMember().getUpiId(),
                    s.getToMember().getDisplayName(),
                    s.getAmountPaise(),
                    s.getNote()
                );
                dto.setUpiUri(standardUri);
                dto.setAppSpecificUri(upiPaymentService.buildAppSpecificUri(
                    s.getUpiApp(),
                    s.getToMember().getUpiId(),
                    s.getToMember().getDisplayName(),
                    s.getAmountPaise(),
                    s.getNote()
                ));
                dto.setQrSvg(upiPaymentService.generateQrSvg(standardUri, 220));
            } catch (Exception ignored) {
                // If recipient UPI ID is not yet validly formatted, omit URI
            }
        }

        return dto;
    }

    /**
     * Preview mutual offsetting expenses that can get cancelled with each other.
     */
    @Transactional(readOnly = true)
    public SettleUpPreviewDto previewSettleUp(Long groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        List<ExpenseEntity> cancellingExpenses = findCancellingExpenses(groupId);
        long totalPaise = 0L;
        List<CancelledExpenseDto> dtos = new ArrayList<>();
        for (ExpenseEntity exp : cancellingExpenses) {
            totalPaise += exp.getAmountPaise();
            dtos.add(new CancelledExpenseDto(
                exp.getId(),
                exp.getDescription(),
                exp.getAmountPaise(),
                exp.getPaidByMember().getId(),
                exp.getPaidByMember().getDisplayName()
            ));
        }

        String message = cancellingExpenses.isEmpty()
            ? "No mutual offsetting expenses found to cancel."
            : String.format("Found %d mutual offsetting expense%s totaling ₹%.2f that can be cancelled.",
                cancellingExpenses.size(),
                cancellingExpenses.size() == 1 ? "" : "s",
                totalPaise / 100.0);

        return new SettleUpPreviewDto(
            groupId,
            cancellingExpenses.size(),
            dtos,
            totalPaise,
            message
        );
    }

    /**
     * Execute Settle Up: Cancel and remove mutual offsetting expenses, void orphan settlements,
     * and update Activity History.
     */
    @Transactional
    public SettleUpResultDto executeSettleUp(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        List<ExpenseEntity> cancellingExpenses = findCancellingExpenses(groupId);
        if (cancellingExpenses.isEmpty()) {
            return new SettleUpResultDto(
                groupId,
                0,
                Collections.emptyList(),
                0L,
                "No mutual offsetting expenses found to cancel. All current balances require direct settlement.",
                false
            );
        }

        long totalPaise = 0L;
        List<CancelledExpenseDto> cancelledDtos = new ArrayList<>();
        StringBuilder expenseDetails = new StringBuilder();

        for (int i = 0; i < cancellingExpenses.size(); i++) {
            ExpenseEntity exp = cancellingExpenses.get(i);
            totalPaise += exp.getAmountPaise();
            cancelledDtos.add(new CancelledExpenseDto(
                exp.getId(),
                exp.getDescription(),
                exp.getAmountPaise(),
                exp.getPaidByMember().getId(),
                exp.getPaidByMember().getDisplayName()
            ));

            if (i > 0) expenseDetails.append(" and ");
            expenseDetails.append(String.format("'%s' (₹%.2f by %s)",
                exp.getDescription(),
                exp.getAmountPaise() / 100.0,
                exp.getPaidByMember().getDisplayName()
            ));

            expenseRepository.delete(exp);
        }
        expenseRepository.flush();

        // Check if any expenses remain in group
        List<ExpenseEntity> remainingExpenses = expenseRepository.findByGroupId(groupId);
        boolean fullySettled = remainingExpenses.isEmpty();

        // If no expenses remain in group, void any active/pending settlements so net balances reach 0.00
        if (fullySettled) {
            List<SettlementEntity> activeSettlements = settlementRepository.findByGroupIdAndStatusIn(
                groupId,
                List.of(SettlementStatus.RECORDED, SettlementStatus.REPORTED_SUCCESS, SettlementStatus.INITIATED, SettlementStatus.PENDING_CONFIRMATION)
            );
            for (SettlementEntity s : activeSettlements) {
                s.setStatus(SettlementStatus.VOIDED);
                settlementRepository.save(s);
            }
        }

        String summary = String.format(
            "Settle Up executed: Cancelled and removed %d mutual offsetting expense%s: %s. Balances %s.",
            cancellingExpenses.size(),
            cancellingExpenses.size() == 1 ? "" : "s",
            expenseDetails.toString(),
            fullySettled ? "fully settled" : "recalculated"
        );

        activityLogService.logActivity(
            group,
            "SETTLEMENT",
            null,
            ActivityEventType.SETTLEMENT_RECORDED,
            summary
        );

        return new SettleUpResultDto(
            groupId,
            cancellingExpenses.size(),
            cancelledDtos,
            totalPaise,
            summary,
            fullySettled
        );
    }

    /**
     * Finds mutual offsetting expenses between group members.
     */
    public List<ExpenseEntity> findCancellingExpenses(Long groupId) {
        List<ExpenseEntity> allExpenses = expenseRepository.findByGroupId(groupId);
        if (allExpenses.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> cancelledIds = new LinkedHashSet<>();

        // 1. Exact 1-to-1 matching: expenses with equal amount paid by different members
        for (int i = 0; i < allExpenses.size(); i++) {
            ExpenseEntity e1 = allExpenses.get(i);
            if (cancelledIds.contains(e1.getId())) continue;

            for (int j = i + 1; j < allExpenses.size(); j++) {
                ExpenseEntity e2 = allExpenses.get(j);
                if (cancelledIds.contains(e2.getId())) continue;

                if (!e1.getPaidByMember().getId().equals(e2.getPaidByMember().getId())
                        && e1.getAmountPaise().equals(e2.getAmountPaise())) {
                    cancelledIds.add(e1.getId());
                    cancelledIds.add(e2.getId());
                    break;
                }
            }
        }

        // 2. Multi-expense subset matching between pairs of members for remaining uncancelled expenses
        List<ExpenseEntity> remaining = allExpenses.stream()
                .filter(e -> !cancelledIds.contains(e.getId()))
                .collect(Collectors.toList());

        if (!remaining.isEmpty()) {
            Map<Long, List<ExpenseEntity>> byPayer = remaining.stream()
                    .collect(Collectors.groupingBy(e -> e.getPaidByMember().getId()));

            List<Long> payers = new ArrayList<>(byPayer.keySet());
            for (int i = 0; i < payers.size(); i++) {
                Long p1 = payers.get(i);
                List<ExpenseEntity> list1 = byPayer.get(p1).stream()
                        .filter(e -> !cancelledIds.contains(e.getId()))
                        .collect(Collectors.toList());
                if (list1.isEmpty()) continue;

                for (int j = i + 1; j < payers.size(); j++) {
                    Long p2 = payers.get(j);
                    List<ExpenseEntity> list2 = byPayer.get(p2).stream()
                            .filter(e -> !cancelledIds.contains(e.getId()))
                            .collect(Collectors.toList());
                    if (list2.isEmpty()) continue;

                    findEqualSubsets(list1, list2, cancelledIds);
                }
            }
        }

        // 3. For 2-member group, if all remaining expenses have equal sum across both members
        List<ExpenseEntity> stillRemaining = allExpenses.stream()
                .filter(e -> !cancelledIds.contains(e.getId()))
                .collect(Collectors.toList());

        if (!stillRemaining.isEmpty()) {
            Map<Long, Long> sumByPayer = stillRemaining.stream()
                    .collect(Collectors.groupingBy(e -> e.getPaidByMember().getId(),
                            Collectors.summingLong(ExpenseEntity::getAmountPaise)));
            if (sumByPayer.size() == 2) {
                Iterator<Long> iter = sumByPayer.values().iterator();
                Long sum1 = iter.next();
                Long sum2 = iter.next();
                if (sum1.equals(sum2)) {
                    for (ExpenseEntity e : stillRemaining) {
                        cancelledIds.add(e.getId());
                    }
                }
            }
        }

        return allExpenses.stream()
                .filter(e -> cancelledIds.contains(e.getId()))
                .collect(Collectors.toList());
    }

    private void findEqualSubsets(List<ExpenseEntity> list1, List<ExpenseEntity> list2, Set<Long> cancelledIds) {
        if (list1.size() > 12 || list2.size() > 12) {
            long sum1 = list1.stream().mapToLong(ExpenseEntity::getAmountPaise).sum();
            long sum2 = list2.stream().mapToLong(ExpenseEntity::getAmountPaise).sum();
            if (sum1 == sum2) {
                list1.forEach(e -> cancelledIds.add(e.getId()));
                list2.forEach(e -> cancelledIds.add(e.getId()));
            }
            return;
        }

        Map<Long, List<ExpenseEntity>> subsetSums1 = computeSubsetSums(list1);
        Map<Long, List<ExpenseEntity>> subsetSums2 = computeSubsetSums(list2);

        for (Map.Entry<Long, List<ExpenseEntity>> entry1 : subsetSums1.entrySet()) {
            Long amount = entry1.getKey();
            if (subsetSums2.containsKey(amount)) {
                List<ExpenseEntity> s1 = entry1.getValue();
                List<ExpenseEntity> s2 = subsetSums2.get(amount);

                boolean conflict = s1.stream().anyMatch(e -> cancelledIds.contains(e.getId()))
                        || s2.stream().anyMatch(e -> cancelledIds.contains(e.getId()));
                if (!conflict) {
                    s1.forEach(e -> cancelledIds.add(e.getId()));
                    s2.forEach(e -> cancelledIds.add(e.getId()));
                }
            }
        }
    }

    private Map<Long, List<ExpenseEntity>> computeSubsetSums(List<ExpenseEntity> list) {
        Map<Long, List<ExpenseEntity>> result = new HashMap<>();
        int n = list.size();
        int totalSubsets = 1 << n;
        for (int mask = 1; mask < totalSubsets; mask++) {
            long sum = 0L;
            List<ExpenseEntity> subset = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    ExpenseEntity e = list.get(i);
                    sum += e.getAmountPaise();
                    subset.add(e);
                }
            }
            if (!result.containsKey(sum) || subset.size() < result.get(sum).size()) {
                result.put(sum, subset);
            }
        }
        return result;
    }
}
