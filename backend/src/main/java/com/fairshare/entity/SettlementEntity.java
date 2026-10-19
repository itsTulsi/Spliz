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

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SettlementEntity() {}

    public SettlementEntity(GroupEntity group, GroupMemberEntity fromMember, GroupMemberEntity toMember,
                            Long amountPaise, String note, Instant paidAt) {
        this.group = group;
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.amountPaise = amountPaise;
        this.note = note;
        this.paidAt = paidAt != null ? paidAt : Instant.now();
        this.status = SettlementStatus.RECORDED;
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

    public Instant getCreatedAt() { return createdAt; }
}
