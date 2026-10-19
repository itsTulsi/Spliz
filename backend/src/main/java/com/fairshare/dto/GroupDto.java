package com.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class GroupDto {
    private Long id;

    @NotBlank(message = "Group name is required")
    @Size(max = 120, message = "Group name cannot exceed 120 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private String currencyCode;
    private Integer memberCount;
    private Instant createdAt;

    public GroupDto() {}

    public GroupDto(Long id, String name, String description, String currencyCode, Integer memberCount, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
