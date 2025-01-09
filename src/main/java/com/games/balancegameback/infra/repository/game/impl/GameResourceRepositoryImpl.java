package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.infra.entity.GameResourcesEntity;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameResourceRepositoryImpl implements GameResourceRepository {

    private final GameResourceJpaRepository gameResourceJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(GameResources gameResources) {
        gameResourceJpaRepository.save(GameResourcesEntity.from(gameResources));
    }
}
