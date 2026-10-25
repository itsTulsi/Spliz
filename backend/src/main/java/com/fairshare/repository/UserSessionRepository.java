package com.fairshare.repository;

import com.fairshare.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {
    Optional<UserSessionEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
