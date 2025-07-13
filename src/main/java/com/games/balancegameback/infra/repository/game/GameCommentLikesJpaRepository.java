package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameCommentLikesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameCommentLikesJpaRepository extends JpaRepository<GameCommentLikesEntity, Long> {

    void deleteByUsersEmailAndResourceCommentsId(String email, Long commentId);

    void deleteByUsersEmailAndResultCommentsId(String email, Long commentId);

    boolean existsByUsersEmailAndResourceCommentsId(String email, Long commentId);

    boolean existsByUsersEmailAndResultCommentsId(String email, Long commentId);

    @Modifying
    @Query("DELETE FROM GameCommentLikesEntity gcl WHERE " +
            "gcl.resourceComments.gameResources.games.id IN :gameIds OR " +
            "gcl.resultComments.games.id IN :gameIds")
    void deleteByGameIds(@Param("gameIds") List<Long> gameIds);
}
