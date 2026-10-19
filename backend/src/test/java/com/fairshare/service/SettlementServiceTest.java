package com.fairshare.service;

import com.fairshare.dto.ConfirmSettlementPaymentRequest;
import com.fairshare.dto.InitiateUpiPaymentRequest;
import com.fairshare.dto.RecordSettlementRequest;
import com.fairshare.dto.SettlementResponseDto;
import com.fairshare.entity.ExpenseEntity;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.enums.SettlementStatus;
import com.fairshare.exception.BusinessException;
import com.fairshare.repository.ActivityLogRepository;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import com.fairshare.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private ActivityLogRepository activityLogRepository;
    @Mock
    private com.fairshare.repository.ExpenseRepository expenseRepository;

    private ActivityLogService activityLogService;
    private UpiPaymentService upiPaymentService;
    private SettlementService settlementService;

    private GroupEntity group;
    private GroupMemberEntity rahul;
    private GroupMemberEntity priyanshi;

    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService(activityLogRepository);
        upiPaymentService = new UpiPaymentService();
        settlementService = new SettlementService(
                settlementRepository,
                groupRepository,
                groupMemberRepository,
                activityLogService,
                upiPaymentService,
                expenseRepository
        );

        group = new GroupEntity("Trip", "Desc", "INR");
        group.setId(1L);

        rahul = new GroupMemberEntity(group, "Rahul", "rahul@okhdfcbank");
        rahul.setId(10L);

        priyanshi = new GroupMemberEntity(group, "Priyanshi", "priyanshi@okaxis");
        priyanshi.setId(20L);
    }

    @Test
    @DisplayName("Should prevent settlement where payer and recipient are identical")
    void testSamePayerAndRecipientThrowsException() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        RecordSettlementRequest req = new RecordSettlementRequest();
        req.setFromMemberId(10L);
        req.setToMemberId(10L);
        req.setAmountPaise(50000L);

        assertThrows(BusinessException.class, () -> settlementService.recordSettlement(1L, req));
    }

    @Test
    @DisplayName("Should successfully initiate UPI payment and return payment URIs")
    void testInitiateUpiPaymentSuccess() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByIdAndGroupId(10L, 1L)).thenReturn(Optional.of(rahul));
        when(groupMemberRepository.findByIdAndGroupId(20L, 1L)).thenReturn(Optional.of(priyanshi));

        when(settlementRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(invocation -> {
            SettlementEntity s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        InitiateUpiPaymentRequest req = new InitiateUpiPaymentRequest();
        req.setFromMemberId(10L);
        req.setToMemberId(20L);
        req.setAmountPaise(60000L);
        req.setPaymentApp("GPAY");
        req.setNote("Dinner share");

        SettlementResponseDto dto = settlementService.initiateUpiPayment(1L, req);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(SettlementStatus.INITIATED, dto.getStatus());
        assertEquals("UPI", dto.getPaymentMethod());
        assertEquals("GPAY", dto.getUpiApp());
        assertNotNull(dto.getUpiUri());
        assertTrue(dto.getUpiUri().contains("pa=priyanshi%40okaxis"));
        assertTrue(dto.getAppSpecificUri().startsWith("gpay://upi/pay"));
    }

    @Test
    @DisplayName("Should fail initiation if recipient has no UPI ID and none is provided")
    void testInitiateUpiPaymentMissingUpiIdThrowsException() {
        priyanshi.setUpiId(null);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByIdAndGroupId(10L, 1L)).thenReturn(Optional.of(rahul));
        when(groupMemberRepository.findByIdAndGroupId(20L, 1L)).thenReturn(Optional.of(priyanshi));

        InitiateUpiPaymentRequest req = new InitiateUpiPaymentRequest();
        req.setFromMemberId(10L);
        req.setToMemberId(20L);
        req.setAmountPaise(60000L);

        assertThrows(BusinessException.class, () -> settlementService.initiateUpiPayment(1L, req));
    }

    @Test
    @DisplayName("Should allow inline UPI ID configuration when initiating payment")
    void testInitiateUpiPaymentWithInlineUpiId() {
        priyanshi.setUpiId(null);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByIdAndGroupId(10L, 1L)).thenReturn(Optional.of(rahul));
        when(groupMemberRepository.findByIdAndGroupId(20L, 1L)).thenReturn(Optional.of(priyanshi));
        when(settlementRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(invocation -> {
            SettlementEntity s = invocation.getArgument(0);
            s.setId(101L);
            return s;
        });

        InitiateUpiPaymentRequest req = new InitiateUpiPaymentRequest();
        req.setFromMemberId(10L);
        req.setToMemberId(20L);
        req.setAmountPaise(60000L);
        req.setRecipientUpiId("priyanshi.new@okicici");

        SettlementResponseDto dto = settlementService.initiateUpiPayment(1L, req);

        assertNotNull(dto);
        assertEquals("priyanshi.new@okicici", priyanshi.getUpiId());
        verify(groupMemberRepository).save(priyanshi);
    }

    @Test
    @DisplayName("Should confirm settlement as REPORTED_SUCCESS and record audit log")
    void testConfirmSettlementPaymentSuccess() {
        SettlementEntity initiated = new SettlementEntity(
                group, rahul, priyanshi, 60000L, "Note", null, SettlementStatus.INITIATED, "UPI", "GPAY", "key-123"
        );
        initiated.setId(200L);

        when(settlementRepository.findByIdAndGroupId(200L, 1L)).thenReturn(Optional.of(initiated));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(i -> i.getArgument(0));

        ConfirmSettlementPaymentRequest req = new ConfirmSettlementPaymentRequest(
                SettlementStatus.REPORTED_SUCCESS,
                "UPI1234567890",
                "Paid via Google Pay"
        );

        SettlementResponseDto dto = settlementService.confirmSettlementPayment(1L, 200L, req);

        assertNotNull(dto);
        assertEquals(SettlementStatus.REPORTED_SUCCESS, dto.getStatus());
        assertEquals("UPI1234567890", dto.getTransactionRef());
        verify(activityLogRepository).save(any());
    }

    @Test
    @DisplayName("Should handle payment cancellation gracefully")
    void testConfirmSettlementPaymentCancelled() {
        SettlementEntity initiated = new SettlementEntity(
                group, rahul, priyanshi, 60000L, "Note", null, SettlementStatus.INITIATED, "UPI", "PHONEPE", "key-456"
        );
        initiated.setId(201L);

        when(settlementRepository.findByIdAndGroupId(201L, 1L)).thenReturn(Optional.of(initiated));
        when(settlementRepository.save(any(SettlementEntity.class))).thenAnswer(i -> i.getArgument(0));

        ConfirmSettlementPaymentRequest req = new ConfirmSettlementPaymentRequest(
                SettlementStatus.CANCELLED,
                null,
                "User backed out"
        );

        SettlementResponseDto dto = settlementService.confirmSettlementPayment(1L, 201L, req);

        assertEquals(SettlementStatus.CANCELLED, dto.getStatus());
        verifyNoInteractions(activityLogRepository);
    }

    @Test
    @DisplayName("Should be idempotent if settlement is already confirmed")
    void testIdempotentConfirmation() {
        SettlementEntity alreadyConfirmed = new SettlementEntity(
                group, rahul, priyanshi, 60000L, "Note", null, SettlementStatus.REPORTED_SUCCESS, "UPI", "PAYTM", "key-789"
        );
        alreadyConfirmed.setId(202L);

        when(settlementRepository.findByIdAndGroupId(202L, 1L)).thenReturn(Optional.of(alreadyConfirmed));

        ConfirmSettlementPaymentRequest req = new ConfirmSettlementPaymentRequest(
                SettlementStatus.REPORTED_SUCCESS,
                "REF",
                null
        );

        SettlementResponseDto dto = settlementService.confirmSettlementPayment(1L, 202L, req);

        assertEquals(SettlementStatus.REPORTED_SUCCESS, dto.getStatus());
        verify(settlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should detect mutual cancelling expenses and execute settle up")
    void testMutualExpenseSettleUp() {
        ExpenseEntity exp1 = new ExpenseEntity(group, "Split 1", 100000L, rahul, com.fairshare.enums.SplitMethod.EQUAL, null);
        exp1.setId(101L);
        ExpenseEntity exp2 = new ExpenseEntity(group, "Split 2", 100000L, priyanshi, com.fairshare.enums.SplitMethod.EQUAL, null);
        exp2.setId(102L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroupId(1L)).thenReturn(List.of(exp1, exp2));

        com.fairshare.dto.SettleUpPreviewDto preview = settlementService.previewSettleUp(1L);
        assertEquals(2, preview.getCancellableExpensesCount());
        assertEquals(200000L, preview.getTotalAmountCancelledPaise());

        com.fairshare.dto.SettleUpResultDto result = settlementService.executeSettleUp(1L);
        assertEquals(2, result.getCancelledExpensesCount());
        verify(expenseRepository).delete(exp1);
        verify(expenseRepository).delete(exp2);
        verify(activityLogRepository).save(any());
    }
}
