package com.fairshare.repository;

import com.fairshare.entity.ActivityLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {
    List<ActivityLogEntity> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @Modifying
    @Query("DELETE FROM ActivityLogEntity a WHERE a.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
