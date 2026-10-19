package com.fairshare.controller;

import com.fairshare.dto.CreateExpenseRequest;
import com.fairshare.dto.ExpenseResponseDto;
import com.fairshare.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getGroupExpenses(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<ExpenseResponseDto> createExpense(@PathVariable Long groupId,
                                                           @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponseDto created = expenseService.createExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(@PathVariable Long expenseId,
                                                           @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
