package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResourceCommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResourceCommentJpaRepository extends JpaRepository<GameResourceCommentsEntity, Long> {

}
