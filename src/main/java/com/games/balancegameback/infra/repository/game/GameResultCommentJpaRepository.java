package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResultCommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameResultCommentJpaRepository extends JpaRepository<GameResultCommentsEntity, Long> {

    Optional<GameResultCommentsEntity> findById(Long id);
}
