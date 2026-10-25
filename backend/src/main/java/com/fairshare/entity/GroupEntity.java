package com.fairshare.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode = "INR";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserEntity owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMemberEntity> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseEntity> expenses = new ArrayList<>();

    public GroupEntity() {}

    public GroupEntity(String name, String description, String currencyCode) {
        this.name = name;
        this.description = description;
        if (currencyCode != null) {
            this.currencyCode = currencyCode;
        }
    }

    public GroupEntity(String name, String description, String currencyCode, UserEntity owner) {
        this.name = name;
        this.description = description;
        if (currencyCode != null) {
            this.currencyCode = currencyCode;
        }
        this.owner = owner;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public UserEntity getOwner() { return owner; }
    public void setOwner(UserEntity owner) { this.owner = owner; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<GroupMemberEntity> getMembers() { return members; }
    public void setMembers(List<GroupMemberEntity> members) { this.members = members; }

    public List<ExpenseEntity> getExpenses() { return expenses; }
    public void setExpenses(List<ExpenseEntity> expenses) { this.expenses = expenses; }
}
