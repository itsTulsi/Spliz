package com.fairshare.service;

import com.fairshare.dto.BalanceSummaryDto;
import com.fairshare.dto.MemberBalanceDto;
import com.fairshare.dto.SettlementSuggestionResponseDto;
import com.fairshare.dto.TransferDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SettlementOptimizationService {

    private static final int EXACT_SEARCH_LIMIT = 8;

    private final BalanceService balanceService;

    public SettlementOptimizationService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public SettlementSuggestionResponseDto optimizeSettlements(Long groupId) {
        BalanceSummaryDto summary = balanceService.calculateGroupBalances(groupId);

        // Filter out zero balances
        Map<Long, Long> activeBalances = new LinkedHashMap<>();
        Map<Long, String> memberNames = new HashMap<>();

        for (MemberBalanceDto b : summary.getBalances()) {
            memberNames.put(b.getMemberId(), b.getDisplayName());
            if (b.getNetBalancePaise() != 0) {
                activeBalances.put(b.getMemberId(), b.getNetBalancePaise());
            }
        }

        if (activeBalances.isEmpty()) {
            return new SettlementSuggestionResponseDto(groupId, "INR", "EXACT_OPTIMAL", Collections.emptyList());
        }

        String algorithm;
        List<TransferDto> transfers;

        if (activeBalances.size() <= EXACT_SEARCH_LIMIT) {
            algorithm = "EXACT_OPTIMAL";
            transfers = solveExactOptimal(activeBalances, memberNames);
        } else {
            algorithm = "GREEDY_HEURISTIC";
            transfers = solveGreedy(activeBalances, memberNames);
        }

        return new SettlementSuggestionResponseDto(groupId, "INR", algorithm, transfers);
    }

    /**
     * Greedy matching algorithm: matches largest debtor with largest creditor.
     */
    public List<TransferDto> solveGreedy(Map<Long, Long> balances, Map<Long, String> memberNames) {
        PriorityQueue<MemberDebt> debtors = new PriorityQueue<>(Comparator.comparingLong(d -> d.amount));
        PriorityQueue<MemberDebt> creditors = new PriorityQueue<>(Comparator.comparingLong(c -> -c.amount));

        for (Map.Entry<Long, Long> entry : balances.entrySet()) {
            if (entry.getValue() < 0) {
                debtors.offer(new MemberDebt(entry.getKey(), -entry.getValue()));
            } else if (entry.getValue() > 0) {
                creditors.offer(new MemberDebt(entry.getKey(), entry.getValue()));
            }
        }

        List<TransferDto> transfers = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            MemberDebt debtor = debtors.poll();
            MemberDebt creditor = creditors.poll();

            long settleAmount = Math.min(debtor.amount, creditor.amount);

            String debtorName = memberNames.getOrDefault(debtor.memberId, "Member #" + debtor.memberId);
            String creditorName = memberNames.getOrDefault(creditor.memberId, "Member #" + creditor.memberId);
            String explanation = String.format("%s pays %s ₹%.2f to settle debt.", debtorName, creditorName, settleAmount / 100.0);

            transfers.add(new TransferDto(
                debtor.memberId,
                debtorName,
                creditor.memberId,
                creditorName,
                settleAmount,
                explanation
            ));

            if (debtor.amount > settleAmount) {
                debtors.offer(new MemberDebt(debtor.memberId, debtor.amount - settleAmount));
            }
            if (creditor.amount > settleAmount) {
                creditors.offer(new MemberDebt(creditor.memberId, creditor.amount - settleAmount));
            }
        }

        return transfers;
    }

    /**
     * Exact search algorithm for small groups: guarantees minimal transaction count.
     */
    public List<TransferDto> solveExactOptimal(Map<Long, Long> balances, Map<Long, String> memberNames) {
        // Backtracking / recursive branch-and-bound search
        List<TransferDto> bestPlan = new ArrayList<>();
        int[] minTransfers = new int[]{Integer.MAX_VALUE};

        List<Long> memberIds = new ArrayList<>(balances.keySet());
        long[] currentBalances = new long[memberIds.size()];
        for (int i = 0; i < memberIds.size(); i++) {
            currentBalances[i] = balances.get(memberIds.get(i));
        }

        backtrackOptimal(currentBalances, 0, new ArrayList<>(), bestPlan, minTransfers, memberIds, memberNames);

        return bestPlan.isEmpty() ? solveGreedy(balances, memberNames) : bestPlan;
    }

    private void backtrackOptimal(long[] balances, int startIndex, List<TransferDto> currentTransfers,
                                  List<TransferDto> bestPlan, int[] minTransfers,
                                  List<Long> memberIds, Map<Long, String> memberNames) {
        // Find next non-zero balance
        while (startIndex < balances.length && balances[startIndex] == 0) {
            startIndex++;
        }

        if (startIndex == balances.length) {
            if (currentTransfers.size() < minTransfers[0]) {
                minTransfers[0] = currentTransfers.size();
                bestPlan.clear();
                bestPlan.addAll(currentTransfers);
            }
            return;
        }

        if (currentTransfers.size() >= minTransfers[0]) {
            return; // Prune branch
        }

        long firstVal = balances[startIndex];
        for (int j = startIndex + 1; j < balances.length; j++) {
            // Pair with an opposite sign balance
            if ((firstVal < 0 && balances[j] > 0) || (firstVal > 0 && balances[j] < 0)) {
                long amount = Math.min(Math.abs(firstVal), Math.abs(balances[j]));
                Long debtorId = firstVal < 0 ? memberIds.get(startIndex) : memberIds.get(j);
                Long creditorId = firstVal < 0 ? memberIds.get(j) : memberIds.get(startIndex);

                balances[startIndex] += (firstVal < 0 ? amount : -amount);
                balances[j] += (firstVal < 0 ? -amount : amount);

                String debtorName = memberNames.getOrDefault(debtorId, "Member #" + debtorId);
                String creditorName = memberNames.getOrDefault(creditorId, "Member #" + creditorId);
                String explanation = String.format("%s pays %s ₹%.2f (Optimal transfer).", debtorName, creditorName, amount / 100.0);

                TransferDto transfer = new TransferDto(debtorId, debtorName, creditorId, creditorName, amount, explanation);
                currentTransfers.add(transfer);

                backtrackOptimal(balances, startIndex, currentTransfers, bestPlan, minTransfers, memberIds, memberNames);

                // Backtrack state
                currentTransfers.remove(currentTransfers.size() - 1);
                balances[startIndex] -= (firstVal < 0 ? amount : -amount);
                balances[j] -= (firstVal < 0 ? -amount : amount);
            }
        }
    }

    private static class MemberDebt {
        final Long memberId;
        final Long amount;

        MemberDebt(Long memberId, Long amount) {
            this.memberId = memberId;
            this.amount = amount;
        }
    }
}
