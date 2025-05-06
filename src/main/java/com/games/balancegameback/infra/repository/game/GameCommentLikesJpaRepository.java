package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameCommentLikesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCommentLikesJpaRepository extends JpaRepository<GameCommentLikesEntity, Long> {

    void deleteByUsersEmailAndResourceCommentsId(String email, Long commentId);

    void deleteByUsersEmailAndResultCommentsId(String email, Long commentId);

    boolean existsByUsersEmailAndResourceCommentsId(String email, Long commentId);

    boolean existsByUsersEmailAndResultCommentsId(String email, Long commentId);

    void deleteByUsersUid(String uid);
}
