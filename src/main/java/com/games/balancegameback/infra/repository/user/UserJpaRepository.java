package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UsersEntity, String> {

    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByNickname(String nickname);

    boolean existsByEmailAndIsDeleted(String email, boolean deleted);

    boolean existsByNickname(String nickname);

    boolean existsByUid(String uid);
}
