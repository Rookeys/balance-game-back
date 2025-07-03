package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.UsersEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UsersEntity, String> {

    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    List<UsersEntity> findAllByIsDeletedTrueAndCreatedDateBefore(OffsetDateTime dateTime);

    void delete(UsersEntity users);

    @Modifying
    @Query("UPDATE UsersEntity u SET u.nickname = :anonymousNickname WHERE u.uid = :uid")
    void anonymizeUserPersonalInfo(@Param("uid") String uid, @Param("anonymousNickname") String anonymousNickname);
}
