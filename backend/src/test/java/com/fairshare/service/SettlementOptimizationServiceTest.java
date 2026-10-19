package com.fairshare.service;

import com.fairshare.dto.TransferDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SettlementOptimizationServiceTest {

    private SettlementOptimizationService optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new SettlementOptimizationService(null);
    }

    @Test
    @DisplayName("Greedy matching generates valid transfers that clear all debts and credits")
    void testGreedyMatchingInvariants() {
        // Balances:
        // Member 1 (A): +60000 paise (+₹600)
        // Member 2 (B): -30000 paise (-₹300)
        // Member 3 (C): -30000 paise (-₹300)
        Map<Long, Long> balances = new HashMap<>();
        balances.put(1L, 60000L);
        balances.put(2L, -30000L);
        balances.put(3L, -30000L);

        Map<Long, String> names = Map.of(1L, "Alice", 2L, "Bob", 3L, "Charlie");

        List<TransferDto> transfers = optimizer.solveGreedy(balances, names);

        assertNotNull(transfers);
        assertEquals(2, transfers.size());

        // Invariant: sum of transfer amounts must equal total creditor balance
        long totalTransferred = transfers.stream().mapToLong(TransferDto::getAmountPaise).sum();
        assertEquals(60000L, totalTransferred);

        // Invariant: transfers must clear every balance
        Map<Long, Long> remaining = new HashMap<>(balances);
        for (TransferDto t : transfers) {
            assertTrue(t.getAmountPaise() > 0, "Transfer amount must be positive");
            remaining.put(t.getFromMemberId(), remaining.get(t.getFromMemberId()) + t.getAmountPaise());
            remaining.put(t.getToMemberId(), remaining.get(t.getToMemberId()) - t.getAmountPaise());
        }

        for (Long net : remaining.values()) {
            assertEquals(0L, net.longValue(), "Remaining balance after applying plan must be zero");
        }
    }

    @Test
    @DisplayName("Exact optimal search finds the minimum number of transfers for small groups")
    void testExactOptimalMinimizesTransactions() {
        // Suppose:
        // A: +30, B: +20, C: -30, D: -20
        // Optimal is 2 transfers: C -> A 30, and D -> B 20.
        Map<Long, Long> balances = new HashMap<>();
        balances.put(1L, 3000L);
        balances.put(2L, 2000L);
        balances.put(3L, -3000L);
        balances.put(4L, -2000L);

        Map<Long, String> names = Map.of(1L, "A", 2L, "B", 3L, "C", 4L, "D");

        List<TransferDto> transfers = optimizer.solveExactOptimal(balances, names);
        assertEquals(2, transfers.size(), "Exact optimal plan must resolve in exactly 2 transactions");

        // Verify that applying transfers clears all balances
        Map<Long, Long> remaining = new HashMap<>(balances);
        for (TransferDto t : transfers) {
            remaining.put(t.getFromMemberId(), remaining.get(t.getFromMemberId()) + t.getAmountPaise());
            remaining.put(t.getToMemberId(), remaining.get(t.getToMemberId()) - t.getAmountPaise());
        }

        for (Long net : remaining.values()) {
            assertEquals(0L, net.longValue());
        }
    }
}
