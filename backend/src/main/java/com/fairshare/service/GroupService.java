package com.fairshare.service;

import com.fairshare.dto.GroupDto;
import com.fairshare.dto.MemberDto;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ActivityLogService activityLogService;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        ActivityLogService activityLogService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDto getGroupById(Long id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
        return mapToDto(group);
    }

    @Transactional
    public GroupDto createGroup(GroupDto dto) {
        GroupEntity group = new GroupEntity(dto.getName().trim(), dto.getDescription(), dto.getCurrencyCode());
        GroupEntity saved = groupRepository.save(group);

        activityLogService.logActivity(saved, "GROUP", saved.getId(),
            ActivityEventType.GROUP_CREATED, "Group '" + saved.getName() + "' created.");

        return mapToDto(saved);
    }

    @Transactional
    public GroupDto updateGroup(Long id, GroupDto dto) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
        group.setName(dto.getName().trim());
        group.setDescription(dto.getDescription());
        if (dto.getCurrencyCode() != null) {
            group.setCurrencyCode(dto.getCurrencyCode());
        }
        return mapToDto(groupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Group not found with ID: " + id);
        }
        groupRepository.deleteById(id);
    }

    // Member Management
    @Transactional(readOnly = true)
    public List<MemberDto> getGroupMembers(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with ID: " + groupId);
        }
        return groupMemberRepository.findByGroupIdOrderByDisplayNameAsc(groupId).stream()
                .map(m -> new MemberDto(m.getId(), m.getGroup().getId(), m.getDisplayName(), m.getJoinedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDto addMemberToGroup(Long groupId, MemberDto dto) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        String name = dto.getDisplayName().trim();
        if (groupMemberRepository.existsByGroupIdAndDisplayNameIgnoreCase(groupId, name)) {
            throw new BusinessException("A member named '" + name + "' already exists in this group.");
        }

        GroupMemberEntity member = new GroupMemberEntity(group, name);
        GroupMemberEntity saved = groupMemberRepository.save(member);

        activityLogService.logActivity(group, "MEMBER", saved.getId(),
            ActivityEventType.MEMBER_ADDED, "Member '" + saved.getDisplayName() + "' joined the group.");

        return new MemberDto(saved.getId(), group.getId(), saved.getDisplayName(), saved.getJoinedAt());
    }

    private GroupDto mapToDto(GroupEntity g) {
        return new GroupDto(
            g.getId(),
            g.getName(),
            g.getDescription(),
            g.getCurrencyCode(),
            g.getMembers() != null ? g.getMembers().size() : 0,
            g.getCreatedAt()
        );
    }
}
