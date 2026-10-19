package com.fairshare.dto;

import com.fairshare.enums.SettlementStatus;
import java.time.Instant;

public class SettlementResponseDto {
    private Long id;
    private Long groupId;
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private String toMemberUpiId;
    private Long amountPaise;
    private SettlementStatus status;
    private String paymentMethod;
    private String upiApp;
    private String upiUri;
    private String appSpecificUri;
    private String idempotencyKey;
    private String transactionRef;
    private String note;
    private String qrSvg;
    private Instant paidAt;
    private Instant createdAt;

    public SettlementResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }

    public String getFromMemberName() { return fromMemberName; }
    public void setFromMemberName(String fromMemberName) { this.fromMemberName = fromMemberName; }

    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }

    public String getToMemberName() { return toMemberName; }
    public void setToMemberName(String toMemberName) { this.toMemberName = toMemberName; }

    public String getToMemberUpiId() { return toMemberUpiId; }
    public void setToMemberUpiId(String toMemberUpiId) { this.toMemberUpiId = toMemberUpiId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getUpiApp() { return upiApp; }
    public void setUpiApp(String upiApp) { this.upiApp = upiApp; }

    public String getUpiUri() { return upiUri; }
    public void setUpiUri(String upiUri) { this.upiUri = upiUri; }

    public String getAppSpecificUri() { return appSpecificUri; }
    public void setAppSpecificUri(String appSpecificUri) { this.appSpecificUri = appSpecificUri; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getQrSvg() { return qrSvg; }
    public void setQrSvg(String qrSvg) { this.qrSvg = qrSvg; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
