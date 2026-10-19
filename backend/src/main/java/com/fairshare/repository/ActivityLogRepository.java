package com.fairshare.repository;

import com.fairshare.entity.ActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {
    List<ActivityLogEntity> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
