/*
 * File Name   : GamePlayCountCacheRepository.java
 * Description : 게임별 플레이 카운트 캐시 조회 Repository (Spring AOP self-invocation 방지용 분리 빈)
 *
 * Created By  : cheomuk
 * Created At  : 2026-05-04
 * Updated At  : 2026-05-04
 *
 * Change Log
 * -------------------------------------------------
 * 2026-05-04  최초 생성
 */
package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.infra.repository.game.common.GameConstants;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class GamePlayCountCacheRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Cacheable(value = "game-total-plays", key = "#gameId")
    public int getGameTotalPlayCount(Long gameId) {
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    @Cacheable(value = "game-week-plays", key = "#gameId")
    public int getGameWeekPlayCount(Long gameId) {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneWeekAgo)))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    @Cacheable(value = "game-month-plays", key = "#gameId")
    public int getGameMonthPlayCount(Long gameId) {
        OffsetDateTime oneMonthAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1);
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneMonthAgo)))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }
}
