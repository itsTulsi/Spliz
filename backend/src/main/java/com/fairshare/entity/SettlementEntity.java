package com.fairshare.entity;

import com.fairshare.enums.SettlementStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "settlements")
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_member_id", nullable = false)
    private GroupMemberEntity fromMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_member_id", nullable = false)
    private GroupMemberEntity toMember;

    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status = SettlementStatus.RECORDED;

    @Column(length = 255)
    private String note;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "MANUAL";

    @Column(name = "upi_app", length = 50)
    private String upiApp;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public SettlementEntity() {}

    public SettlementEntity(GroupEntity group, GroupMemberEntity fromMember, GroupMemberEntity toMember,
                            Long amountPaise, String note, Instant paidAt) {
        this(group, fromMember, toMember, amountPaise, note, paidAt, SettlementStatus.RECORDED, "MANUAL", null, null);
    }

    public SettlementEntity(GroupEntity group, GroupMemberEntity fromMember, GroupMemberEntity toMember,
                            Long amountPaise, String note, Instant paidAt, SettlementStatus status,
                            String paymentMethod, String upiApp, String idempotencyKey) {
        this.group = group;
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.amountPaise = amountPaise;
        this.note = note;
        this.paidAt = paidAt != null ? paidAt : Instant.now();
        this.status = status != null ? status : SettlementStatus.RECORDED;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "MANUAL";
        this.upiApp = upiApp;
        this.idempotencyKey = idempotencyKey;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupEntity getGroup() { return group; }
    public void setGroup(GroupEntity group) { this.group = group; }

    public GroupMemberEntity getFromMember() { return fromMember; }
    public void setFromMember(GroupMemberEntity fromMember) { this.fromMember = fromMember; }

    public GroupMemberEntity getToMember() { return toMember; }
    public void setToMember(GroupMemberEntity toMember) { this.toMember = toMember; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getUpiApp() { return upiApp; }
    public void setUpiApp(String upiApp) { this.upiApp = upiApp; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
