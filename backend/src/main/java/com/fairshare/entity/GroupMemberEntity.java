package com.fairshare.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "group_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "display_name"})
})
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public GroupMemberEntity() {}

    public GroupMemberEntity(GroupEntity group, String displayName) {
        this(group, displayName, null, null);
    }

    public GroupMemberEntity(GroupEntity group, String displayName, String upiId) {
        this(group, displayName, upiId, null);
    }

    public GroupMemberEntity(GroupEntity group, String displayName, String upiId, UserEntity user) {
        this.group = group;
        this.displayName = displayName;
        this.upiId = upiId;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupEntity getGroup() { return group; }
    public void setGroup(GroupEntity group) { this.group = group; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public Instant getJoinedAt() { return joinedAt; }
}
