package com.fairshare.repository;

import com.fairshare.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {
    List<ExpenseEntity> findByGroupIdOrderByOccurredAtDesc(Long groupId);
    List<ExpenseEntity> findByGroupId(Long groupId);
}
