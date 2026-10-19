package com.fairshare.dto;

public class MemberBalanceDto {
    private Long memberId;
    private String displayName;
    private Long paidExpensesPaise;
    private Long sharesOwedPaise;
    private Long settlementsPaidPaise;
    private Long settlementsReceivedPaise;
    private Long netBalancePaise;

    public MemberBalanceDto() {}

    public MemberBalanceDto(Long memberId, String displayName, Long paidExpensesPaise,
                            Long sharesOwedPaise, Long settlementsPaidPaise,
                            Long settlementsReceivedPaise, Long netBalancePaise) {
        this.memberId = memberId;
        this.displayName = displayName;
        this.paidExpensesPaise = paidExpensesPaise;
        this.sharesOwedPaise = sharesOwedPaise;
        this.settlementsPaidPaise = settlementsPaidPaise;
        this.settlementsReceivedPaise = settlementsReceivedPaise;
        this.netBalancePaise = netBalancePaise;
    }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Long getPaidExpensesPaise() { return paidExpensesPaise; }
    public void setPaidExpensesPaise(Long paidExpensesPaise) { this.paidExpensesPaise = paidExpensesPaise; }

    public Long getSharesOwedPaise() { return sharesOwedPaise; }
    public void setSharesOwedPaise(Long sharesOwedPaise) { this.sharesOwedPaise = sharesOwedPaise; }

    public Long getSettlementsPaidPaise() { return settlementsPaidPaise; }
    public void setSettlementsPaidPaise(Long settlementsPaidPaise) { this.settlementsPaidPaise = settlementsPaidPaise; }

    public Long getSettlementsReceivedPaise() { return settlementsReceivedPaise; }
    public void setSettlementsReceivedPaise(Long settlementsReceivedPaise) { this.settlementsReceivedPaise = settlementsReceivedPaise; }

    public Long getNetBalancePaise() { return netBalancePaise; }
    public void setNetBalancePaise(Long netBalancePaise) { this.netBalancePaise = netBalancePaise; }
}
