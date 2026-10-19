package com.fairshare.dto;

import java.util.List;

public class SettlementSuggestionResponseDto {
    private Long groupId;
    private String currency;
    private String algorithm; // EXACT_OPTIMAL or GREEDY_HEURISTIC
    private List<TransferDto> transfers;
    private Integer transferCount;

    public SettlementSuggestionResponseDto() {}

    public SettlementSuggestionResponseDto(Long groupId, String currency, String algorithm,
                                           List<TransferDto> transfers) {
        this.groupId = groupId;
        this.currency = currency;
        this.algorithm = algorithm;
        this.transfers = transfers;
        this.transferCount = transfers != null ? transfers.size() : 0;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public List<TransferDto> getTransfers() { return transfers; }
    public void setTransfers(List<TransferDto> transfers) {
        this.transfers = transfers;
        this.transferCount = transfers != null ? transfers.size() : 0;
    }

    public Integer getTransferCount() { return transferCount; }
    public void setTransferCount(Integer transferCount) { this.transferCount = transferCount; }
}
