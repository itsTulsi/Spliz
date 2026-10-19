package com.fairshare.controller;

import com.fairshare.dto.PreviewSplitRequest;
import com.fairshare.dto.PreviewSplitResponse;
import com.fairshare.service.FairnessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class FairnessSimulatorController {

    private final FairnessService fairnessService;

    public FairnessSimulatorController(FairnessService fairnessService) {
        this.fairnessService = fairnessService;
    }

    @PostMapping("/{expenseId}/preview-split")
    public ResponseEntity<PreviewSplitResponse> previewSplit(@PathVariable Long expenseId,
                                                             @Valid @RequestBody PreviewSplitRequest request) {
        return ResponseEntity.ok(fairnessService.previewSplit(expenseId, request));
    }

    @PutMapping("/{expenseId}/apply-split")
    public ResponseEntity<Void> applySplit(@PathVariable Long expenseId,
                                           @Valid @RequestBody PreviewSplitRequest request) {
        fairnessService.applySplit(expenseId, request);
        return ResponseEntity.ok().build();
    }
}
