package com.fairshare.entity;

import com.fairshare.enums.SplitMethod;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by_member_id", nullable = false)
    private GroupMemberEntity paidByMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_method", nullable = false, length = 30)
    private SplitMethod splitMethod = SplitMethod.EQUAL;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShareEntity> shares = new ArrayList<>();

    public ExpenseEntity() {}

    public ExpenseEntity(GroupEntity group, String description, Long amountPaise,
                         GroupMemberEntity paidByMember, SplitMethod splitMethod, Instant occurredAt) {
        this.group = group;
        this.description = description;
        this.amountPaise = amountPaise;
        this.paidByMember = paidByMember;
        this.splitMethod = splitMethod != null ? splitMethod : SplitMethod.EQUAL;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupEntity getGroup() { return group; }
    public void setGroup(GroupEntity group) { this.group = group; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public GroupMemberEntity getPaidByMember() { return paidByMember; }
    public void setPaidByMember(GroupMemberEntity paidByMember) { this.paidByMember = paidByMember; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<ExpenseShareEntity> getShares() { return shares; }
    public void setShares(List<ExpenseShareEntity> shares) { this.shares = shares; }
}
