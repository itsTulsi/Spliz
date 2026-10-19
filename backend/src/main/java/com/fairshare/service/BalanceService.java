package com.fairshare.service;

import com.fairshare.dto.BalanceSummaryDto;
import com.fairshare.dto.MemberBalanceDto;
import com.fairshare.entity.ExpenseEntity;
import com.fairshare.entity.ExpenseShareEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.SettlementStatus;
import com.fairshare.repository.ExpenseRepository;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import com.fairshare.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BalanceService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;

    public BalanceService(GroupRepository groupRepository,
                          GroupMemberRepository groupMemberRepository,
                          ExpenseRepository expenseRepository,
                          SettlementRepository settlementRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public BalanceSummaryDto calculateGroupBalances(Long groupId) {
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupIdOrderByIdAsc(groupId);
        List<ExpenseEntity> expenses = expenseRepository.findByGroupId(groupId);
        List<SettlementEntity> settlements = settlementRepository.findByGroupIdAndStatus(groupId, SettlementStatus.RECORDED);

        Map<Long, Long> paidExpenses = new HashMap<>();
        Map<Long, Long> sharesOwed = new HashMap<>();
        Map<Long, Long> settlementsPaid = new HashMap<>();
        Map<Long, Long> settlementsReceived = new HashMap<>();

        for (GroupMemberEntity m : members) {
            paidExpenses.put(m.getId(), 0L);
            sharesOwed.put(m.getId(), 0L);
            settlementsPaid.put(m.getId(), 0L);
            settlementsReceived.put(m.getId(), 0L);
        }

        // Tally paid expenses and allocated shares
        for (ExpenseEntity exp : expenses) {
            Long payerId = exp.getPaidByMember().getId();
            paidExpenses.put(payerId, paidExpenses.getOrDefault(payerId, 0L) + exp.getAmountPaise());

            for (ExpenseShareEntity share : exp.getShares()) {
                Long shareMemberId = share.getMember().getId();
                sharesOwed.put(shareMemberId, sharesOwed.getOrDefault(shareMemberId, 0L) + share.getSharePaise());
            }
        }

        // Tally recorded settlements
        for (SettlementEntity s : settlements) {
            Long fromId = s.getFromMember().getId();
            Long toId = s.getToMember().getId();

            settlementsPaid.put(fromId, settlementsPaid.getOrDefault(fromId, 0L) + s.getAmountPaise());
            settlementsReceived.put(toId, settlementsReceived.getOrDefault(toId, 0L) + s.getAmountPaise());
        }

        List<MemberBalanceDto> balanceDtos = new ArrayList<>();
        long totalNetSum = 0;

        for (GroupMemberEntity m : members) {
            long paid = paidExpenses.getOrDefault(m.getId(), 0L);
            long owed = sharesOwed.getOrDefault(m.getId(), 0L);
            long sPaid = settlementsPaid.getOrDefault(m.getId(), 0L);
            long sRecv = settlementsReceived.getOrDefault(m.getId(), 0L);

            long net = paid - owed + sPaid - sRecv;
            totalNetSum += net;

            balanceDtos.add(new MemberBalanceDto(
                m.getId(),
                m.getDisplayName(),
                paid,
                owed,
                sPaid,
                sRecv,
                net
            ));
        }

        // Invariant check: sum of net balances must equal 0 in closed financial groups
        if (totalNetSum != 0) {
            throw new IllegalStateException("Data integrity error: Group sum of net balances is non-zero: " + totalNetSum);
        }

        return new BalanceSummaryDto(groupId, "INR", balanceDtos, totalNetSum);
    }
}
