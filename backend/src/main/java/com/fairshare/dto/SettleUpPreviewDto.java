package com.fairshare.dto;

import java.util.List;

public class SettleUpPreviewDto {
    private Long groupId;
    private int cancellableExpensesCount;
    private List<CancelledExpenseDto> cancellableExpenses;
    private long totalAmountCancelledPaise;
    private String message;

    public SettleUpPreviewDto() {}

    public SettleUpPreviewDto(Long groupId, int cancellableExpensesCount,
                              List<CancelledExpenseDto> cancellableExpenses,
                              long totalAmountCancelledPaise, String message) {
        this.groupId = groupId;
        this.cancellableExpensesCount = cancellableExpensesCount;
        this.cancellableExpenses = cancellableExpenses;
        this.totalAmountCancelledPaise = totalAmountCancelledPaise;
        this.message = message;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public int getCancellableExpensesCount() { return cancellableExpensesCount; }
    public void setCancellableExpensesCount(int cancellableExpensesCount) { this.cancellableExpensesCount = cancellableExpensesCount; }

    public List<CancelledExpenseDto> getCancellableExpenses() { return cancellableExpenses; }
    public void setCancellableExpenses(List<CancelledExpenseDto> cancellableExpenses) { this.cancellableExpenses = cancellableExpenses; }

    public long getTotalAmountCancelledPaise() { return totalAmountCancelledPaise; }
    public void setTotalAmountCancelledPaise(long totalAmountCancelledPaise) { this.totalAmountCancelledPaise = totalAmountCancelledPaise; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
