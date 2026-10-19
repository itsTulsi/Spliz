package com.fairshare.dto;

import com.fairshare.enums.SettlementStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConfirmSettlementPaymentRequest {

    @NotNull(message = "Confirmation status is required")
    private SettlementStatus status; // REPORTED_SUCCESS, CANCELLED, FAILED

    @Size(max = 100, message = "Transaction reference cannot exceed 100 characters")
    private String transactionRef;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    public ConfirmSettlementPaymentRequest() {}

    public ConfirmSettlementPaymentRequest(SettlementStatus status, String transactionRef, String note) {
        this.status = status;
        this.transactionRef = transactionRef;
        this.note = note;
    }

    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
