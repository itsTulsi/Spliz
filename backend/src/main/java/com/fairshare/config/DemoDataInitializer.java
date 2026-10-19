package com.fairshare.config;

import com.fairshare.dto.CreateExpenseRequest;
import com.fairshare.dto.GroupDto;
import com.fairshare.dto.MemberDto;
import com.fairshare.enums.SplitMethod;
import com.fairshare.repository.GroupRepository;
import com.fairshare.service.ExpenseService;
import com.fairshare.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

/**
 * Initializes deterministic seed data for presentation and evaluation if the database is clean.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final ExpenseService expenseService;

    public DemoDataInitializer(GroupRepository groupRepository,
                               GroupService groupService,
                               ExpenseService expenseService) {
        this.groupRepository = groupRepository;
        this.groupService = groupService;
        this.expenseService = expenseService;
    }

    @Override
    public void run(String... args) {
        if (groupRepository.count() > 0) {
            log.info("Database already contains records. Skipping seed data generation.");
            return;
        }

        log.info("Populating initial demonstration data for evaluation...");

        // 1. Create College Trip Group
        GroupDto group = groupService.createGroup(new GroupDto(
            null,
            "Goa Trip 2026",
            "College batch trip expenses, beach shack dinners, and fuel sharing.",
            "INR",
            0,
            Instant.now()
        ));

        // 2. Add 4 Members
        MemberDto rahul = groupService.addMemberToGroup(group.getId(), new MemberDto(null, group.getId(), "Rahul", null));
        MemberDto priyanshi = groupService.addMemberToGroup(group.getId(), new MemberDto(null, group.getId(), "Priyanshi", null));
        MemberDto amit = groupService.addMemberToGroup(group.getId(), new MemberDto(null, group.getId(), "Amit", null));
        MemberDto sneha = groupService.addMemberToGroup(group.getId(), new MemberDto(null, group.getId(), "Sneha", null));

        // 3. Record Initial Shared Expenses
        // Expense 1: Beach Shack Dinner - ₹2,400.00 (240000 paise) paid by Rahul for all 4 people (₹600 each)
        CreateExpenseRequest exp1 = new CreateExpenseRequest();
        exp1.setDescription("Beach Shack Seafood Dinner");
        exp1.setAmountPaise(240000L);
        exp1.setPaidByMemberId(rahul.getId());
        exp1.setSplitMethod(SplitMethod.EQUAL);
        exp1.setParticipantMemberIds(Arrays.asList(rahul.getId(), priyanshi.getId(), amit.getId(), sneha.getId()));
        exp1.setOccurredAt(Instant.now().minusSeconds(86400));
        expenseService.createExpense(group.getId(), exp1);

        // Expense 2: Self-drive Car Rental & Fuel - ₹4,000.00 (400000 paise) paid by Priyanshi for all 4 people
        CreateExpenseRequest exp2 = new CreateExpenseRequest();
        exp2.setDescription("Rental Car & Fuel");
        exp2.setAmountPaise(400000L);
        exp2.setPaidByMemberId(priyanshi.getId());
        exp2.setSplitMethod(SplitMethod.EQUAL);
        exp2.setParticipantMemberIds(Arrays.asList(rahul.getId(), priyanshi.getId(), amit.getId(), sneha.getId()));
        exp2.setOccurredAt(Instant.now().minusSeconds(43200));
        expenseService.createExpense(group.getId(), exp2);

        // Expense 3: Scuba Diving Activity - ₹3,000.00 (300000 paise) paid by Amit for Rahul, Priyanshi, and Amit only
        CreateExpenseRequest exp3 = new CreateExpenseRequest();
        exp3.setDescription("Scuba Diving Tickets");
        exp3.setAmountPaise(300000L);
        exp3.setPaidByMemberId(amit.getId());
        exp3.setSplitMethod(SplitMethod.EQUAL);
        exp3.setParticipantMemberIds(Arrays.asList(rahul.getId(), priyanshi.getId(), amit.getId()));
        exp3.setOccurredAt(Instant.now().minusSeconds(14400));
        expenseService.createExpense(group.getId(), exp3);

        log.info("Demo data created successfully for Group ID: {}", group.getId());
    }
}
