package com.fairshare.dto;

import jakarta.validation.constraints.NotNull;

public class ShareInputDto {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    private Long sharePaise;
    private Integer percentageBasisPoints;

    public ShareInputDto() {}

    public ShareInputDto(Long memberId, Long sharePaise) {
        this.memberId = memberId;
        this.sharePaise = sharePaise;
    }

    public ShareInputDto(Long memberId, Long sharePaise, Integer percentageBasisPoints) {
        this.memberId = memberId;
        this.sharePaise = sharePaise;
        this.percentageBasisPoints = percentageBasisPoints;
    }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getSharePaise() { return sharePaise; }
    public void setSharePaise(Long sharePaise) { this.sharePaise = sharePaise; }

    public Integer getPercentageBasisPoints() { return percentageBasisPoints; }
    public void setPercentageBasisPoints(Integer percentageBasisPoints) { this.percentageBasisPoints = percentageBasisPoints; }
}
