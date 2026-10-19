package com.fairshare.dto;

import com.fairshare.enums.SplitMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class CreateExpenseRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @NotNull(message = "Total amount is required")
    @Min(value = 1, message = "Amount must be a positive integer in paise")
    private Long amountPaise;

    @NotNull(message = "Payer member ID is required")
    private Long paidByMemberId;

    private SplitMethod splitMethod = SplitMethod.EQUAL;
    private Instant occurredAt;

    private List<Long> participantMemberIds; // For EQUAL split
    private List<ShareInputDto> shares;      // For EXACT or PERCENTAGE split

    public CreateExpenseRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public Long getPaidByMemberId() { return paidByMemberId; }
    public void setPaidByMemberId(Long paidByMemberId) { this.paidByMemberId = paidByMemberId; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public List<Long> getParticipantMemberIds() { return participantMemberIds; }
    public void setParticipantMemberIds(List<Long> participantMemberIds) { this.participantMemberIds = participantMemberIds; }

    public List<ShareInputDto> getShares() { return shares; }
    public void setShares(List<ShareInputDto> shares) { this.shares = shares; }
}
