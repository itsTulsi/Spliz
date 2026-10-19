package com.fairshare.dto;

public class TransferDto {
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private Long amountPaise;
    private String explanation;

    public TransferDto() {}

    public TransferDto(Long fromMemberId, String fromMemberName, Long toMemberId,
                       String toMemberName, Long amountPaise, String explanation) {
        this.fromMemberId = fromMemberId;
        this.fromMemberName = fromMemberName;
        this.toMemberId = toMemberId;
        this.toMemberName = toMemberName;
        this.amountPaise = amountPaise;
        this.explanation = explanation;
    }

    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }

    public String getFromMemberName() { return fromMemberName; }
    public void setFromMemberName(String fromMemberName) { this.fromMemberName = fromMemberName; }

    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }

    public String getToMemberName() { return toMemberName; }
    public void setToMemberName(String toMemberName) { this.toMemberName = toMemberName; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
