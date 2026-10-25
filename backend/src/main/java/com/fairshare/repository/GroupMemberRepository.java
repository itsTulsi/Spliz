package com.fairshare.repository;

import com.fairshare.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {
    List<GroupMemberEntity> findByGroupIdOrderByDisplayNameAsc(Long groupId);
    List<GroupMemberEntity> findByGroupIdOrderByIdAsc(Long groupId);
    boolean existsByGroupIdAndDisplayNameIgnoreCase(Long groupId, String displayName);
    Optional<GroupMemberEntity> findByIdAndGroupId(Long id, Long groupId);
    List<GroupMemberEntity> findByUserId(Long userId);
}
