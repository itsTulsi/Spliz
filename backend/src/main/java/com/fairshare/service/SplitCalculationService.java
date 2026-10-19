package com.fairshare.service;

import com.fairshare.dto.ShareInputDto;
import com.fairshare.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Authoritative financial calculation engine for splitting expenses in integer paise.
 */
@Service
public class SplitCalculationService {

    public static final int TOTAL_BASIS_POINTS = 10000; // 100.00%

    /**
     * Compute equal split across ordered participants with deterministic remainder allocation.
     * First `rem` participants in stable order receive (base + 1) paise; remaining receive base paise.
     */
    public Map<Long, Long> calculateEqualSplit(long totalPaise, List<Long> participantMemberIds) {
        if (totalPaise <= 0) {
            throw new BusinessException("Expense amount must be a positive integer in paise.");
        }
        if (participantMemberIds == null || participantMemberIds.isEmpty()) {
            throw new BusinessException("At least one participant must be selected for equal split.");
        }

        // Enforce distinct participant IDs in deterministic ascending order
        List<Long> orderedParticipants = new ArrayList<>(new LinkedHashSet<>(participantMemberIds));
        Collections.sort(orderedParticipants);

        int n = orderedParticipants.size();
        long baseShare = totalPaise / n;
        long remainder = totalPaise % n;

        Map<Long, Long> allocatedShares = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            Long memberId = orderedParticipants.get(i);
            long share = (i < remainder) ? (baseShare + 1) : baseShare;
            allocatedShares.put(memberId, share);
        }

        // Verify fundamental invariant
        verifySumInvariant(totalPaise, allocatedShares);
        return allocatedShares;
    }

    /**
     * Validate exact amounts split and confirm shares add up to totalPaise.
     */
    public Map<Long, Long> validateAndCalculateExactSplit(long totalPaise, List<ShareInputDto> shares) {
        if (totalPaise <= 0) {
            throw new BusinessException("Expense amount must be a positive integer in paise.");
        }
        if (shares == null || shares.isEmpty()) {
            throw new BusinessException("Exact shares configuration cannot be empty.");
        }

        Map<Long, Long> allocatedShares = new LinkedHashMap<>();
        Set<Long> seenMembers = new HashSet<>();

        for (ShareInputDto s : shares) {
            if (s.getMemberId() == null) {
                throw new BusinessException("Member ID is required for each exact share.");
            }
            if (!seenMembers.add(s.getMemberId())) {
                throw new BusinessException("Duplicate participant ID in exact shares: " + s.getMemberId());
            }
            if (s.getSharePaise() == null || s.getSharePaise() < 0) {
                throw new BusinessException("Share amount must be non-negative for member ID: " + s.getMemberId());
            }
            allocatedShares.put(s.getMemberId(), s.getSharePaise());
        }

        long sumShares = allocatedShares.values().stream().mapToLong(Long::longValue).sum();
        if (sumShares != totalPaise) {
            throw new BusinessException(String.format(
                "Exact split shares sum to %d paise (₹%.2f), but expense total is %d paise (₹%.2f). Discrepancy: %d paise.",
                sumShares, sumShares / 100.0, totalPaise, totalPaise / 100.0, Math.abs(sumShares - totalPaise)
            ));
        }

        return allocatedShares;
    }

    /**
     * Compute percentage split using integer basis points and the largest-remainder (Hamilton) method.
     */
    public Map<Long, Long> calculatePercentageSplit(long totalPaise, List<ShareInputDto> shares) {
        if (totalPaise <= 0) {
            throw new BusinessException("Expense amount must be a positive integer in paise.");
        }
        if (shares == null || shares.isEmpty()) {
            throw new BusinessException("Percentage shares configuration cannot be empty.");
        }

        int totalBasisPoints = 0;
        Set<Long> seenMembers = new HashSet<>();
        for (ShareInputDto s : shares) {
            if (s.getMemberId() == null) {
                throw new BusinessException("Member ID is required for each percentage share.");
            }
            if (!seenMembers.add(s.getMemberId())) {
                throw new BusinessException("Duplicate participant ID in percentage shares: " + s.getMemberId());
            }
            if (s.getPercentageBasisPoints() == null || s.getPercentageBasisPoints() < 0) {
                throw new BusinessException("Percentage basis points must be non-negative for member ID: " + s.getMemberId());
            }
            totalBasisPoints += s.getPercentageBasisPoints();
        }

        if (totalBasisPoints != TOTAL_BASIS_POINTS) {
            throw new BusinessException(String.format(
                "Total percentage must equal exactly 100.00%% (10,000 basis points). Current total: %.2f%% (%d basis points).",
                totalBasisPoints / 100.0, totalBasisPoints
            ));
        }

        // Step 1: Compute initial floor shares and fractional remainders
        Map<Long, Long> initialShares = new LinkedHashMap<>();
        Map<Long, Long> fractionalRemainders = new HashMap<>();

        for (ShareInputDto s : shares) {
            long numerator = totalPaise * s.getPercentageBasisPoints();
            long floorShare = numerator / TOTAL_BASIS_POINTS;
            long remainder = numerator % TOTAL_BASIS_POINTS;

            initialShares.put(s.getMemberId(), floorShare);
            fractionalRemainders.put(s.getMemberId(), remainder);
        }

        // Step 2: Calculate unallocated paise
        long allocatedSum = initialShares.values().stream().mapToLong(Long::longValue).sum();
        long unallocatedPaise = totalPaise - allocatedSum;

        // Step 3: Sort participants by fractional remainder descending, tie-breaking on memberId ascending
        List<Long> rankedMembers = new ArrayList<>(initialShares.keySet());
        rankedMembers.sort((m1, m2) -> {
            int cmp = Long.compare(fractionalRemainders.get(m2), fractionalRemainders.get(m1));
            return cmp != 0 ? cmp : Long.compare(m1, m2);
        });

        // Step 4: Award 1 paise to top unallocated participants
        for (int i = 0; i < unallocatedPaise; i++) {
            Long recipient = rankedMembers.get(i);
            initialShares.put(recipient, initialShares.get(recipient) + 1);
        }

        // Verify fundamental invariant
        verifySumInvariant(totalPaise, initialShares);
        return initialShares;
    }

    private void verifySumInvariant(long totalPaise, Map<Long, Long> shares) {
        long sum = shares.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalPaise) {
            throw new IllegalStateException(String.format(
                "Critical financial calculation invariant violated: sum of shares (%d) does not match total amount (%d)",
                sum, totalPaise
            ));
        }
    }
}
