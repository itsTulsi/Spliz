package com.fairshare.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InitiateUpiPaymentRequest {

    @NotNull(message = "From member ID is required")
    @JsonAlias({"debtorMemberId", "payerMemberId", "debtorId", "fromId"})
    private Long fromMemberId;

    @NotNull(message = "To member ID is required")
    @JsonAlias({"creditorMemberId", "recipientMemberId", "creditorId", "toId"})
    private Long toMemberId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be a positive integer in paise")
    private Long amountPaise;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    @JsonAlias({"targetUpiApp", "upiApp", "app"})
    private String paymentApp; // GPAY, PHONEPE, PAYTM, BHIM, GENERIC_UPI

    private String idempotencyKey;

    @JsonAlias({"vpa", "upiId", "recipientUpi"})
    private String recipientUpiId; // Optional: configure or update recipient UPI ID inline

    public InitiateUpiPaymentRequest() {}

    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }

    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPaymentApp() { return paymentApp; }
    public void setPaymentApp(String paymentApp) { this.paymentApp = paymentApp; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRecipientUpiId() { return recipientUpiId; }
    public void setRecipientUpiId(String recipientUpiId) { this.recipientUpiId = recipientUpiId; }

    // Aliases for compatibility
    public void setDebtorMemberId(Long debtorMemberId) {
        if (this.fromMemberId == null) this.fromMemberId = debtorMemberId;
    }
    public void setCreditorMemberId(Long creditorMemberId) {
        if (this.toMemberId == null) this.toMemberId = creditorMemberId;
    }
    public void setTargetUpiApp(String targetUpiApp) {
        if (this.paymentApp == null) this.paymentApp = targetUpiApp;
    }
}
