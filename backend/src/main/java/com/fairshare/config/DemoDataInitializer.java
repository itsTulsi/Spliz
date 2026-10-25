package com.fairshare.config;

import com.fairshare.dto.CreateExpenseRequest;
import com.fairshare.dto.GroupDto;
import com.fairshare.dto.MemberDto;
import com.fairshare.entity.GroupEntity;
import com.fairshare.entity.GroupMemberEntity;
import com.fairshare.entity.UserEntity;
import com.fairshare.enums.SplitMethod;
import com.fairshare.repository.GroupMemberRepository;
import com.fairshare.repository.GroupRepository;
import com.fairshare.repository.UserRepository;
import com.fairshare.security.UserContext;
import com.fairshare.service.ExpenseService;
import com.fairshare.service.GroupService;
import com.fairshare.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes deterministic seed users and demo groups for evaluation.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public DemoDataInitializer(GroupRepository groupRepository,
                               GroupMemberRepository groupMemberRepository,
                               GroupService groupService,
                               ExpenseService expenseService,
                               UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupService = groupService;
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Ensure demo users exist
        UserEntity tulsi = seedUserIfAbsent("tulsi", "tulsi@spliz.local", "tulsi123", "Tulsi", "tulsi@okhdfcbank");
        UserEntity kavya = seedUserIfAbsent("kavya", "kavya@spliz.local", "kavya123", "Kavya", "kavya@okaxis");
        UserEntity rahul = seedUserIfAbsent("rahul", "rahul@spliz.local", "rahul123", "Rahul", "rahul@okhdfcbank");
        UserEntity priyanshi = seedUserIfAbsent("priyanshi", "priyanshi@spliz.local", "priyanshi123", "Priyanshi", "priyanshi@okaxis");

        // 2. Link any legacy unowned groups to tulsi if present
        List<GroupEntity> unownedGroups = groupRepository.findAll().stream()
                .filter(g -> g.getOwner() == null)
                .toList();
        for (GroupEntity g : unownedGroups) {
            g.setOwner(tulsi);
            groupRepository.save(g);
        }

        // 3. Populate demo groups if database has no groups
        if (groupRepository.count() == 0) {
            log.info("Populating initial demonstration groups for multi-user evaluation...");

            try {
                // Group 1: Goa Trip 2026 (Owned by Tulsi)
                UserContext.setCurrentUser(tulsi);
                GroupDto goaGroup = groupService.createGroup(new GroupDto(
                        null,
                        "Goa Trip 2026",
                        "College batch trip expenses, beach shack dinners, and fuel sharing.",
                        "INR",
                        0,
                        Instant.now()
                ));

                MemberDto rahulMember = groupService.addMemberToGroup(goaGroup.getId(), new MemberDto(null, goaGroup.getId(), "Rahul", "rahul@okhdfcbank", null));
                MemberDto priyanshiMember = groupService.addMemberToGroup(goaGroup.getId(), new MemberDto(null, goaGroup.getId(), "Priyanshi", "priyanshi@okaxis", null));
                MemberDto amitMember = groupService.addMemberToGroup(goaGroup.getId(), new MemberDto(null, goaGroup.getId(), "Amit", "amit@ibl", null));
                MemberDto snehaMember = groupService.addMemberToGroup(goaGroup.getId(), new MemberDto(null, goaGroup.getId(), "Sneha", "sneha@ybl", null));

                // Goa trip expenses
                CreateExpenseRequest exp1 = new CreateExpenseRequest();
                exp1.setDescription("Beach Shack Seafood Dinner");
                exp1.setAmountPaise(240000L);
                exp1.setPaidByMemberId(rahulMember.getId());
                exp1.setSplitMethod(SplitMethod.EQUAL);
                exp1.setParticipantMemberIds(Arrays.asList(rahulMember.getId(), priyanshiMember.getId(), amitMember.getId(), snehaMember.getId()));
                exp1.setOccurredAt(Instant.now().minusSeconds(86400));
                expenseService.createExpense(goaGroup.getId(), exp1);

                CreateExpenseRequest exp2 = new CreateExpenseRequest();
                exp2.setDescription("Rental Car & Fuel");
                exp2.setAmountPaise(400000L);
                exp2.setPaidByMemberId(priyanshiMember.getId());
                exp2.setSplitMethod(SplitMethod.EQUAL);
                exp2.setParticipantMemberIds(Arrays.asList(rahulMember.getId(), priyanshiMember.getId(), amitMember.getId(), snehaMember.getId()));
                exp2.setOccurredAt(Instant.now().minusSeconds(43200));
                expenseService.createExpense(goaGroup.getId(), exp2);

                CreateExpenseRequest exp3 = new CreateExpenseRequest();
                exp3.setDescription("Scuba Diving Tickets");
                exp3.setAmountPaise(300000L);
                exp3.setPaidByMemberId(amitMember.getId());
                exp3.setSplitMethod(SplitMethod.EQUAL);
                exp3.setParticipantMemberIds(Arrays.asList(rahulMember.getId(), priyanshiMember.getId(), amitMember.getId()));
                exp3.setOccurredAt(Instant.now().minusSeconds(14400));
                expenseService.createExpense(goaGroup.getId(), exp3);

                // Group 2: Bangalore Flat 402 (Owned by Kavya)
                UserContext.setCurrentUser(kavya);
                GroupDto flatGroup = groupService.createGroup(new GroupDto(
                        null,
                        "Bangalore Flat 402",
                        "Apartment maintenance, groceries, and high-speed broadband bills.",
                        "INR",
                        0,
                        Instant.now()
                ));

                MemberDto rohanMember = groupService.addMemberToGroup(flatGroup.getId(), new MemberDto(null, flatGroup.getId(), "Rohan", "rohan@okhdfcbank", null));
                MemberDto priyaMember = groupService.addMemberToGroup(flatGroup.getId(), new MemberDto(null, flatGroup.getId(), "Priya", "priya@icici", null));

                // Find Kavya's auto-created member ID
                Long kavyaMemberId = groupMemberRepository.findByGroupIdOrderByDisplayNameAsc(flatGroup.getId()).stream()
                        .filter(m -> "Kavya".equalsIgnoreCase(m.getDisplayName()))
                        .map(GroupMemberEntity::getId)
                        .findFirst()
                        .orElse(rohanMember.getId());

                CreateExpenseRequest expFlat = new CreateExpenseRequest();
                expFlat.setDescription("High-Speed Fiber Broadband & Groceries");
                expFlat.setAmountPaise(360000L);
                expFlat.setPaidByMemberId(kavyaMemberId);
                expFlat.setSplitMethod(SplitMethod.EQUAL);
                expFlat.setParticipantMemberIds(Arrays.asList(kavyaMemberId, rohanMember.getId(), priyaMember.getId()));
                expFlat.setOccurredAt(Instant.now().minusSeconds(28800));
                expenseService.createExpense(flatGroup.getId(), expFlat);

                log.info("Demo data created successfully with 2 distinct user-owned groups.");
            } finally {
                UserContext.clear();
            }
        }
    }

    private UserEntity seedUserIfAbsent(String username, String email, String rawPassword, String displayName, String upiId) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    String passwordHash = PasswordUtils.hashPassword(rawPassword);
                    UserEntity user = new UserEntity(username, email, passwordHash, displayName, upiId);
                    UserEntity saved = userRepository.save(user);
                    log.info("Seeded demo user '{}' (id: {})", username, saved.getId());
                    return saved;
                });
    }
}
