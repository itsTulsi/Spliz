package com.fairshare.dto;

import com.fairshare.enums.SplitMethod;
import java.time.Instant;
import java.util.List;

public class ExpenseResponseDto {
    private Long id;
    private Long groupId;
    private String description;
    private Long amountPaise;
    private Long paidByMemberId;
    private String paidByMemberName;
    private SplitMethod splitMethod;
    private Instant occurredAt;
    private Instant createdAt;
    private List<ShareResponseDto> shares;

    public ExpenseResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public Long getPaidByMemberId() { return paidByMemberId; }
    public void setPaidByMemberId(Long paidByMemberId) { this.paidByMemberId = paidByMemberId; }

    public String getPaidByMemberName() { return paidByMemberName; }
    public void setPaidByMemberName(String paidByMemberName) { this.paidByMemberName = paidByMemberName; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<ShareResponseDto> getShares() { return shares; }
    public void setShares(List<ShareResponseDto> shares) { this.shares = shares; }
}
