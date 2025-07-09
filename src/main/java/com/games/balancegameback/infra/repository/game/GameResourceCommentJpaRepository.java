package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResourceCommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResourceCommentJpaRepository extends JpaRepository<GameResourceCommentsEntity, Long> {

    boolean existsById(Long commentId);

    void deleteByUsersUid(String uid);

    @Modifying
    @Query("DELETE FROM GameResourceCommentsEntity c WHERE c.gameResources.games.id = :gameId")
    void deleteByGamesId(@Param("gameId") Long gameId);
}
