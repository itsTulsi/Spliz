package com.fairshare.dto;

import java.util.List;

public class BalanceSummaryDto {
    private Long groupId;
    private String currency;
    private List<MemberBalanceDto> balances;
    private Long sumOfNetBalancesPaise;

    public BalanceSummaryDto() {}

    public BalanceSummaryDto(Long groupId, String currency, List<MemberBalanceDto> balances, Long sumOfNetBalancesPaise) {
        this.groupId = groupId;
        this.currency = currency;
        this.balances = balances;
        this.sumOfNetBalancesPaise = sumOfNetBalancesPaise;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<MemberBalanceDto> getBalances() { return balances; }
    public void setBalances(List<MemberBalanceDto> balances) { this.balances = balances; }

    public Long getSumOfNetBalancesPaise() { return sumOfNetBalancesPaise; }
    public void setSumOfNetBalancesPaise(Long sumOfNetBalancesPaise) { this.sumOfNetBalancesPaise = sumOfNetBalancesPaise; }
}
