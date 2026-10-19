package com.fairshare.service;

import com.fairshare.dto.ShareInputDto;
import com.fairshare.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SplitCalculationServiceTest {

    private SplitCalculationService splitService;

    @BeforeEach
    void setUp() {
        splitService = new SplitCalculationService();
    }

    @Test
    @DisplayName("Equal split divides cleanly when total is a multiple of participant count")
    void testEqualSplitNoRemainder() {
        // ₹900.00 = 90,000 paise among 3 people
        List<Long> participants = Arrays.asList(1L, 2L, 3L);
        Map<Long, Long> shares = splitService.calculateEqualSplit(90000L, participants);

        assertEquals(3, shares.size());
        assertEquals(30000L, shares.get(1L));
        assertEquals(30000L, shares.get(2L));
        assertEquals(30000L, shares.get(3L));
        assertEquals(90000L, shares.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("Equal split distributes remainder paise deterministically to lowest member IDs")
    void testEqualSplitWithRemainderPaise() {
        // ₹100.00 = 10,000 paise among 3 people: 10000 / 3 = 3333 with remainder 1
        List<Long> participants = Arrays.asList(3L, 1L, 2L); // Unordered input
        Map<Long, Long> shares = splitService.calculateEqualSplit(10000L, participants);

        assertEquals(3, shares.size());
        // Stable sort places member 1 first, receiving base + 1
        assertEquals(3334L, shares.get(1L));
        assertEquals(3333L, shares.get(2L));
        assertEquals(3333L, shares.get(3L));
        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("Exact split accepts valid configurations where shares sum exactly to total")
    void testExactSplitValid() {
        // ₹120.00 = 12,000 paise: 5000 + 4000 + 3000
        List<ShareInputDto> shares = Arrays.asList(
            new ShareInputDto(10L, 5000L),
            new ShareInputDto(20L, 4000L),
            new ShareInputDto(30L, 3000L)
        );

        Map<Long, Long> result = splitService.validateAndCalculateExactSplit(12000L, shares);
        assertEquals(3, result.size());
        assertEquals(5000L, result.get(10L));
        assertEquals(4000L, result.get(20L));
        assertEquals(3000L, result.get(30L));
        assertEquals(12000L, result.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("Exact split rejects discrepancies when shares do not sum to expense amount")
    void testExactSplitMismatchedTotal() {
        List<ShareInputDto> shares = Arrays.asList(
            new ShareInputDto(10L, 5000L),
            new ShareInputDto(20L, 4000L)
            // Sum is 9,000 paise, but total is 10,000 paise
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
            splitService.validateAndCalculateExactSplit(10000L, shares)
        );
        assertTrue(ex.getMessage().contains("Discrepancy"));
    }

    @Test
    @DisplayName("Percentage split using Hamilton method accurately awards remainder paise to largest fractions")
    void testPercentageSplitLargestRemainder() {
        // ₹100.00 = 10,000 paise split 33.33%, 33.33%, 33.34%
        // 3333 bp, 3333 bp, 3334 bp = 10,000 bp
        List<ShareInputDto> shares = Arrays.asList(
            new ShareInputDto(1L, null, 3333),
            new ShareInputDto(2L, null, 3333),
            new ShareInputDto(3L, null, 3334)
        );

        Map<Long, Long> result = splitService.calculatePercentageSplit(10000L, shares);
        assertEquals(3, result.size());
        assertEquals(10000L, result.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(3334L, result.get(3L));
    }

    @Test
    @DisplayName("Percentage split rejects percentages that do not sum to 100%")
    void testPercentageSplitInvalidTotal() {
        List<ShareInputDto> shares = Arrays.asList(
            new ShareInputDto(1L, null, 5000), // 50%
            new ShareInputDto(2L, null, 4000)  // 40% (Total 90%)
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
            splitService.calculatePercentageSplit(10000L, shares)
        );
        assertTrue(ex.getMessage().contains("must equal exactly 100.00%"));
    }
}
