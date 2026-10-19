package com.fairshare.controller;

import com.fairshare.dto.RecordSettlementRequest;
import com.fairshare.dto.SettlementSuggestionResponseDto;
import com.fairshare.entity.SettlementEntity;
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
    public ResponseEntity<List<SettlementEntity>> getGroupSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId));
    }

    @PostMapping
    public ResponseEntity<SettlementEntity> recordSettlement(@PathVariable Long groupId,
                                                             @Valid @RequestBody RecordSettlementRequest request) {
        SettlementEntity created = settlementService.recordSettlement(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<SettlementSuggestionResponseDto> getSettlementSuggestions(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementOptimizationService.optimizeSettlements(groupId));
    }
}
