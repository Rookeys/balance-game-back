/*
 * File Name   : GamePlayCountBatchRepository.java
 * Description : 게임 플레이 카운트 DB 배치 집계 쿼리 Repository
 *
 * Created By  : cheomuk
 * Created At  : 2026-05-10
 * Updated At  : 2026-05-10
 *
 * Change Log
 * -------------------------------------------------
 * 2026-05-10  최초 생성 - 스케줄러 전수 갱신 및 Redis fallback 단건 쿼리 제공
 */
package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.infra.repository.game.common.GameConstants;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GamePlayCountBatchRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Map<Long, Integer> fetchTotalCounts() {
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.accessType.eq(AccessType.PUBLIC))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public Map<Long, Integer> fetchWeekCounts() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneWeekAgo)))
                .where(GameQClasses.games.accessType.eq(AccessType.PUBLIC))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public Map<Long, Integer> fetchMonthCounts() {
        OffsetDateTime oneMonthAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1);
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneMonthAgo)))
                .where(GameQClasses.games.accessType.eq(AccessType.PUBLIC))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public Map<Long, Integer> fetchTotalCountsByIds(List<Long> gameIds) {
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public Map<Long, Integer> fetchWeekCountsByIds(List<Long> gameIds) {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneWeekAgo)))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public Map<Long, Integer> fetchMonthCountsByIds(List<Long> gameIds) {
        OffsetDateTime oneMonthAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1);
        List<Tuple> rows = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneMonthAgo)))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
        return toMap(rows);
    }

    public int fetchTotalCountByGameId(Long gameId) {
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    public int fetchWeekCountByGameId(Long gameId) {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneWeekAgo)))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    public int fetchMonthCountByGameId(Long gameId) {
        OffsetDateTime oneMonthAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1);
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results)
                    .on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneMonthAgo)))
                .where(GameQClasses.games.id.eq(gameId)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    private Map<Long, Integer> toMap(List<Tuple> rows) {
        Map<Long, Integer> result = new HashMap<>();
        for (Tuple row : rows) {
            Long gameId = row.get(GameQClasses.games.id);
            Long count = row.get(GameQClasses.results.count());
            if (gameId != null) result.put(gameId, count != null ? count.intValue() : 0);
        }
        return result;
    }
}
