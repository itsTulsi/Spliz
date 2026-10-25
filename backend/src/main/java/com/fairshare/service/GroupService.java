package com.fairshare.service;

import com.fairshare.dto.GroupDto;
import com.fairshare.dto.MemberDto;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.UserEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.*;
import com.fairshare.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ActivityLogService activityLogService;
    private final UpiPaymentService upiPaymentService;
    private final SettlementRepository settlementRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        ActivityLogService activityLogService,
                        UpiPaymentService upiPaymentService,
                        SettlementRepository settlementRepository,
                        ActivityLogRepository activityLogRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.activityLogService = activityLogService;
        this.upiPaymentService = upiPaymentService;
        this.settlementRepository = settlementRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        UserEntity currentUser = UserContext.getCurrentUser();
        if (currentUser != null) {
            return groupRepository.findDistinctByUserIdOrDisplayName(currentUser.getId(), currentUser.getDisplayName())
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
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
        UserEntity currentUser = UserContext.getCurrentUser();
        GroupEntity group = new GroupEntity(dto.getName().trim(), dto.getDescription(), dto.getCurrencyCode(), currentUser);
        GroupEntity saved = groupRepository.save(group);

        // Automatically enroll the creator as the first member if user is logged in
        if (currentUser != null) {
            GroupMemberEntity creatorMember = new GroupMemberEntity(
                    saved,
                    currentUser.getDisplayName(),
                    currentUser.getUpiId(),
                    currentUser
            );
            groupMemberRepository.save(creatorMember);
        }

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
        settlementRepository.deleteByGroupId(id);
        activityLogRepository.deleteByGroupId(id);
        groupRepository.deleteById(id);
    }

    // Member Management
    @Transactional(readOnly = true)
    public List<MemberDto> getGroupMembers(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with ID: " + groupId);
        }
        return groupMemberRepository.findByGroupIdOrderByDisplayNameAsc(groupId).stream()
                .map(m -> new MemberDto(m.getId(), m.getGroup().getId(), m.getDisplayName(), m.getUpiId(), m.getJoinedAt()))
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

        String upiId = dto.getUpiId() != null && !dto.getUpiId().isBlank() ? dto.getUpiId().trim() : null;
        if (upiId != null) {
            upiPaymentService.validateUpiId(upiId);
        }

        // Check if an existing registered user matches by username, display name, or UPI
        UserEntity matchedUser = userRepository.findByUsernameIgnoreCase(name)
                .or(() -> userRepository.findByDisplayNameIgnoreCase(name))
                .or(() -> upiId != null ? userRepository.findByUpiIdIgnoreCase(upiId) : java.util.Optional.empty())
                .orElse(null);

        GroupMemberEntity member = new GroupMemberEntity(group, name, upiId, matchedUser);
        GroupMemberEntity saved = groupMemberRepository.save(member);

        activityLogService.logActivity(group, "MEMBER", saved.getId(),
            ActivityEventType.MEMBER_ADDED, "Member '" + saved.getDisplayName() + "' joined the group.");

        return new MemberDto(saved.getId(), group.getId(), saved.getDisplayName(), saved.getUpiId(), saved.getJoinedAt());
    }

    @Transactional
    public MemberDto updateMember(Long groupId, Long memberId, MemberDto dto) {
        GroupMemberEntity member = groupMemberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (dto.getDisplayName() != null && !dto.getDisplayName().isBlank()) {
            String newName = dto.getDisplayName().trim();
            if (!newName.equalsIgnoreCase(member.getDisplayName()) &&
                groupMemberRepository.existsByGroupIdAndDisplayNameIgnoreCase(groupId, newName)) {
                throw new BusinessException("A member named '" + newName + "' already exists in this group.");
            }
            member.setDisplayName(newName);
        }

        if (dto.getUpiId() != null) {
            String upiId = dto.getUpiId().trim();
            if (!upiId.isBlank()) {
                upiPaymentService.validateUpiId(upiId);
                member.setUpiId(upiId);
            } else {
                member.setUpiId(null);
            }
        }

        GroupMemberEntity saved = groupMemberRepository.save(member);
        return new MemberDto(saved.getId(), groupId, saved.getDisplayName(), saved.getUpiId(), saved.getJoinedAt());
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
