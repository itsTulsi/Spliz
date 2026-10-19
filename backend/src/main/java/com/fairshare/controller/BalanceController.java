package com.fairshare.controller;

import com.fairshare.dto.BalanceSummaryDto;
import com.fairshare.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<BalanceSummaryDto> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.calculateGroupBalances(groupId));
    }
}
