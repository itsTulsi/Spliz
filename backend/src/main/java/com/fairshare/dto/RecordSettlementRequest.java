package com.fairshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class RecordSettlementRequest {
    @NotNull(message = "From member ID is required")
    private Long fromMemberId;

    @NotNull(message = "To member ID is required")
    private Long toMemberId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be a positive integer in paise")
    private Long amountPaise;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    private Instant paidAt;

    public RecordSettlementRequest() {}

    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }

    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
