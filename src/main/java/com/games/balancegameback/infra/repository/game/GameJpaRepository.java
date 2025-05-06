package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GamesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameJpaRepository extends JpaRepository<GamesEntity, Long> {

    void deleteByUsersUid(String uid);

    List<GamesEntity> findByUsersUid(String uid);
}
