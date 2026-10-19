package com.fairshare.service;

import com.fairshare.dto.RecordSettlementRequest;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.ActivityEventType;
import com.fairshare.enums.SettlementStatus;
import com.fairshare.exception.BusinessException;
import com.fairshare.exception.ResourceNotFoundException;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import com.fairshare.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ActivityLogService activityLogService;

    public SettlementService(SettlementRepository settlementRepository,
                             GroupRepository groupRepository,
                             GroupMemberRepository groupMemberRepository,
                             ActivityLogService activityLogService) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<SettlementEntity> getGroupSettlements(Long groupId) {
        return settlementRepository.findByGroupIdOrderByPaidAtDesc(groupId);
    }

    @Transactional
    public SettlementEntity recordSettlement(Long groupId, RecordSettlementRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        if (request.getFromMemberId().equals(request.getToMemberId())) {
            throw new BusinessException("Settlement payer and recipient must be different group members.");
        }

        GroupMemberEntity fromMember = groupMemberRepository.findByIdAndGroupId(request.getFromMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Payer does not belong to group: " + request.getFromMemberId()));

        GroupMemberEntity toMember = groupMemberRepository.findByIdAndGroupId(request.getToMemberId(), groupId)
                .orElseThrow(() -> new BusinessException("Recipient does not belong to group: " + request.getToMemberId()));

        SettlementEntity settlement = new SettlementEntity(
            group,
            fromMember,
            toMember,
            request.getAmountPaise(),
            request.getNote(),
            request.getPaidAt()
        );

        SettlementEntity saved = settlementRepository.save(settlement);

        activityLogService.logActivity(
            group,
            "SETTLEMENT",
            saved.getId(),
            ActivityEventType.SETTLEMENT_RECORDED,
            String.format("Payment of ₹%.2f recorded: %s paid %s.",
                saved.getAmountPaise() / 100.0,
                fromMember.getDisplayName(),
                toMember.getDisplayName())
        );

        return saved;
    }
}
