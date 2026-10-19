package com.fairshare.dto;

import com.fairshare.enums.SplitMethod;
import java.util.ArrayList;
import java.util.List;

public class PreviewSplitResponse {
    private Long expenseId;
    private String currency = "INR";
    private SplitMethod splitMethod;
    private Long totalAmountPaise;
    private List<ShareResponseDto> currentShares = new ArrayList<>();
    private List<ShareResponseDto> proposedShares = new ArrayList<>();
    private boolean persisted = false;
    private List<String> warnings = new ArrayList<>();

    public PreviewSplitResponse() {}

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public Long getTotalAmountPaise() { return totalAmountPaise; }
    public void setTotalAmountPaise(Long totalAmountPaise) { this.totalAmountPaise = totalAmountPaise; }

    public List<ShareResponseDto> getCurrentShares() { return currentShares; }
    public void setCurrentShares(List<ShareResponseDto> currentShares) { this.currentShares = currentShares; }

    public List<ShareResponseDto> getProposedShares() { return proposedShares; }
    public void setProposedShares(List<ShareResponseDto> proposedShares) { this.proposedShares = proposedShares; }

    public boolean isPersisted() { return persisted; }
    public void setPersisted(boolean persisted) { this.persisted = persisted; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
