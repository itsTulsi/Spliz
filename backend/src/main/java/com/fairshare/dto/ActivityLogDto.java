package com.fairshare.dto;

import com.fairshare.enums.ActivityEventType;
import java.time.Instant;

public class ActivityLogDto {
    private Long id;
    private Long groupId;
    private String entityType;
    private Long entityId;
    private ActivityEventType eventType;
    private String summary;
    private Instant createdAt;

    public ActivityLogDto() {}

    public ActivityLogDto(Long id, Long groupId, String entityType, Long entityId,
                          ActivityEventType eventType, String summary, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.eventType = eventType;
        this.summary = summary;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public ActivityEventType getEventType() { return eventType; }
    public void setEventType(ActivityEventType eventType) { this.eventType = eventType; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
