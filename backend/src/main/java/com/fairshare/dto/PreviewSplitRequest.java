package com.fairshare.dto;

import com.fairshare.enums.SplitMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PreviewSplitRequest {

    @NotNull(message = "Total amount is required")
    @Min(value = 1, message = "Amount must be positive")
    private Long amountPaise;

    private SplitMethod splitMethod = SplitMethod.EQUAL;
    private List<Long> participantMemberIds;
    private List<ShareInputDto> shares;

    public PreviewSplitRequest() {}

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public List<Long> getParticipantMemberIds() { return participantMemberIds; }
    public void setParticipantMemberIds(List<Long> participantMemberIds) { this.participantMemberIds = participantMemberIds; }

    public List<ShareInputDto> getShares() { return shares; }
    public void setShares(List<ShareInputDto> shares) { this.shares = shares; }
}
