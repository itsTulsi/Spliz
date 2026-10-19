package com.fairshare.repository;

import com.fairshare.entity.ExpenseShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShareEntity, Long> {
    List<ExpenseShareEntity> findByExpenseId(Long expenseId);
    void deleteByExpenseId(Long expenseId);
}
