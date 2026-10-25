package com.fairshare.repository;

import com.fairshare.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT g FROM GroupEntity g LEFT JOIN g.members m WHERE g.owner.id = :userId OR m.user.id = :userId OR (m.displayName IS NOT NULL AND LOWER(m.displayName) = LOWER(:displayName)) ORDER BY g.createdAt DESC")
    List<GroupEntity> findDistinctByUserIdOrDisplayName(@Param("userId") Long userId, @Param("displayName") String displayName);
}
