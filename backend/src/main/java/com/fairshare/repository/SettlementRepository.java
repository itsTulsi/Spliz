package com.fairshare.repository;

import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByGroupIdOrderByPaidAtDesc(Long groupId);
    List<SettlementEntity> findByGroupIdAndStatus(Long groupId, SettlementStatus status);
}
