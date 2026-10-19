package com.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class MemberDto {
    private Long id;
    private Long groupId;

    @NotBlank(message = "Member display name is required")
    @Size(max = 80, message = "Member display name cannot exceed 80 characters")
    private String displayName;

    private Instant joinedAt;

    public MemberDto() {}

    public MemberDto(Long id, Long groupId, String displayName, Instant joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.displayName = displayName;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
