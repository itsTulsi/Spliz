package com.fairshare.controller;

import com.fairshare.dto.ConfirmSettlementPaymentRequest;
import com.fairshare.dto.InitiateUpiPaymentRequest;
import com.fairshare.dto.RecordSettlementRequest;
import com.fairshare.dto.SettlementResponseDto;
import com.fairshare.dto.SettlementSuggestionResponseDto;
import com.fairshare.dto.SettleUpPreviewDto;
import com.fairshare.dto.SettleUpResultDto;
import com.fairshare.service.SettlementOptimizationService;
import com.fairshare.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementOptimizationService settlementOptimizationService;

    public SettlementController(SettlementService settlementService,
                                SettlementOptimizationService settlementOptimizationService) {
        this.settlementService = settlementService;
        this.settlementOptimizationService = settlementOptimizationService;
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponseDto>> getGroupSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlementById(@PathVariable Long groupId,
                                                                   @PathVariable Long settlementId) {
        return ResponseEntity.ok(settlementService.getSettlementById(groupId, settlementId));
    }

    @PostMapping
    public ResponseEntity<SettlementResponseDto> recordSettlement(@PathVariable Long groupId,
                                                                  @Valid @RequestBody RecordSettlementRequest request) {
        SettlementResponseDto created = settlementService.recordSettlement(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/initiate")
    public ResponseEntity<SettlementResponseDto> initiateUpiPayment(@PathVariable Long groupId,
                                                                    @Valid @RequestBody InitiateUpiPaymentRequest request) {
        SettlementResponseDto initiated = settlementService.initiateUpiPayment(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(initiated);
    }

    @PostMapping("/{settlementId}/confirm")
    public ResponseEntity<SettlementResponseDto> confirmSettlementPayment(@PathVariable Long groupId,
                                                                          @PathVariable Long settlementId,
                                                                          @Valid @RequestBody ConfirmSettlementPaymentRequest request) {
        SettlementResponseDto confirmed = settlementService.confirmSettlementPayment(groupId, settlementId, request);
        return ResponseEntity.ok(confirmed);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<SettlementSuggestionResponseDto> getSettlementSuggestions(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementOptimizationService.optimizeSettlements(groupId));
    }

    @GetMapping("/settle-up/preview")
    public ResponseEntity<SettleUpPreviewDto> previewSettleUp(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.previewSettleUp(groupId));
    }

    @PostMapping("/settle-up")
    public ResponseEntity<SettleUpResultDto> executeSettleUp(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.executeSettleUp(groupId));
    }
}
