package com.fairshare.dto;

import java.util.List;

public class SettleUpResultDto {
    private Long groupId;
    private int cancelledExpensesCount;
    private List<CancelledExpenseDto> cancelledExpenses;
    private long totalAmountCancelledPaise;
    private String summary;
    private boolean fullySettled;

    public SettleUpResultDto() {}

    public SettleUpResultDto(Long groupId, int cancelledExpensesCount,
                            List<CancelledExpenseDto> cancelledExpenses,
                            long totalAmountCancelledPaise, String summary,
                            boolean fullySettled) {
        this.groupId = groupId;
        this.cancelledExpensesCount = cancelledExpensesCount;
        this.cancelledExpenses = cancelledExpenses;
        this.totalAmountCancelledPaise = totalAmountCancelledPaise;
        this.summary = summary;
        this.fullySettled = fullySettled;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public int getCancelledExpensesCount() { return cancelledExpensesCount; }
    public void setCancelledExpensesCount(int cancelledExpensesCount) { this.cancelledExpensesCount = cancelledExpensesCount; }

    public List<CancelledExpenseDto> getCancelledExpenses() { return cancelledExpenses; }
    public void setCancelledExpenses(List<CancelledExpenseDto> cancelledExpenses) { this.cancelledExpenses = cancelledExpenses; }

    public long getTotalAmountCancelledPaise() { return totalAmountCancelledPaise; }
    public void setTotalAmountCancelledPaise(long totalAmountCancelledPaise) { this.totalAmountCancelledPaise = totalAmountCancelledPaise; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public boolean isFullySettled() { return fullySettled; }
    public void setFullySettled(boolean fullySettled) { this.fullySettled = fullySettled; }
}
