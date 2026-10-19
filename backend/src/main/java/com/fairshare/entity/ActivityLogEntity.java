package com.fairshare.entity;

import com.fairshare.enums.ActivityEventType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "activity_logs")
public class ActivityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ActivityEventType eventType;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ActivityLogEntity() {}

    public ActivityLogEntity(GroupEntity group, String entityType, Long entityId,
                             ActivityEventType eventType, String summary) {
        this.group = group;
        this.entityType = entityType;
        this.entityId = entityId;
        this.eventType = eventType;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupEntity getGroup() { return group; }
    public void setGroup(GroupEntity group) { this.group = group; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public ActivityEventType getEventType() { return eventType; }
    public void setEventType(ActivityEventType eventType) { this.eventType = eventType; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Instant getCreatedAt() { return createdAt; }
}
