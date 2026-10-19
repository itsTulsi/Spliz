package com.fairshare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_shares", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"expense_id", "member_id"})
})
public class ExpenseShareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMemberEntity member;

    @Column(name = "share_paise", nullable = false)
    private Long sharePaise;

    @Column(name = "percentage_basis_points")
    private Integer percentageBasisPoints;

    public ExpenseShareEntity() {}

    public ExpenseShareEntity(ExpenseEntity expense, GroupMemberEntity member, Long sharePaise, Integer percentageBasisPoints) {
        this.expense = expense;
        this.member = member;
        this.sharePaise = sharePaise;
        this.percentageBasisPoints = percentageBasisPoints;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExpenseEntity getExpense() { return expense; }
    public void setExpense(ExpenseEntity expense) { this.expense = expense; }

    public GroupMemberEntity getMember() { return member; }
    public void setMember(GroupMemberEntity member) { this.member = member; }

    public Long getSharePaise() { return sharePaise; }
    public void setSharePaise(Long sharePaise) { this.sharePaise = sharePaise; }

    public Integer getPercentageBasisPoints() { return percentageBasisPoints; }
    public void setPercentageBasisPoints(Integer percentageBasisPoints) { this.percentageBasisPoints = percentageBasisPoints; }
}
