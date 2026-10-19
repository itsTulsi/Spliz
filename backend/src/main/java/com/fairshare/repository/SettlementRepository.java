package com.fairshare.repository;

import com.fairshare.entity.SettlementEntity;
import com.fairshare.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    List<SettlementEntity> findByGroupIdOrderByPaidAtDesc(Long groupId);
    List<SettlementEntity> findByGroupIdAndStatus(Long groupId, SettlementStatus status);
    List<SettlementEntity> findByGroupIdAndStatusIn(Long groupId, Collection<SettlementStatus> statuses);
    Optional<SettlementEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<SettlementEntity> findByIdAndGroupId(Long id, Long groupId);
}
