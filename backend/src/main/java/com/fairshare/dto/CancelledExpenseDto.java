package com.fairshare.dto;

public class CancelledExpenseDto {
    private Long id;
    private String description;
    private Long amountPaise;
    private Long paidByMemberId;
    private String paidByMemberName;

    public CancelledExpenseDto() {}

    public CancelledExpenseDto(Long id, String description, Long amountPaise, Long paidByMemberId, String paidByMemberName) {
        this.id = id;
        this.description = description;
        this.amountPaise = amountPaise;
        this.paidByMemberId = paidByMemberId;
        this.paidByMemberName = paidByMemberName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public Long getPaidByMemberId() { return paidByMemberId; }
    public void setPaidByMemberId(Long paidByMemberId) { this.paidByMemberId = paidByMemberId; }

    public String getPaidByMemberName() { return paidByMemberName; }
    public void setPaidByMemberName(String paidByMemberName) { this.paidByMemberName = paidByMemberName; }
}
