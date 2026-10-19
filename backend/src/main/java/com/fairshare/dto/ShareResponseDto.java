package com.fairshare.dto;

public class ShareResponseDto {
    private Long memberId;
    private String memberName;
    private Long sharePaise;
    private Integer percentageBasisPoints;

    public ShareResponseDto() {}

    public ShareResponseDto(Long memberId, String memberName, Long sharePaise, Integer percentageBasisPoints) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.sharePaise = sharePaise;
        this.percentageBasisPoints = percentageBasisPoints;
    }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getSharePaise() { return sharePaise; }
    public void setSharePaise(Long sharePaise) { this.sharePaise = sharePaise; }

    public Integer getPercentageBasisPoints() { return percentageBasisPoints; }
    public void setPercentageBasisPoints(Integer percentageBasisPoints) { this.percentageBasisPoints = percentageBasisPoints; }
}
