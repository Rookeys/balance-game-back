package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ALL 게임 목록 조회 전략
 * - PRIVATE 가 아닌 모든 게임 조회
 * - 로그인 시 내가 만든 PRIVATE 게임도 포함
 * - 최소 2개 이상의 리소스 필요
 */
@Slf4j
@Component
public class AllGamesStrategy extends AbstractGameListStrategy {
    
    @Override
    public BooleanBuilder buildFilterConditions(GameSearchRequest request, Users user) {
        BooleanBuilder builder = new BooleanBuilder();

        if (user != null) {
            // 로그인 시 PRIVATE 가 아닌 게임 or 내가 만든 게임
            BooleanExpression notPrivate = GameQClasses.games.accessType.ne(AccessType.PRIVATE);
            BooleanExpression myGames = GameQClasses.games.users.uid.eq(user.getUid());
            builder.and(notPrivate.or(myGames));
            log.debug("All games filter applied with user: {}", user.getUid());
        } else {
            // 비로그인 시 PRIVATE 가 아닌 게임만
            builder.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE));
            log.debug("All games filter applied without user (non-private only)");
        }

        if (Boolean.TRUE.equals(request.getFollowingOnly())) {
            BooleanExpression followingCondition = buildFollowingCondition(user);
            if (followingCondition != null) {
                builder.and(followingCondition);
                log.debug("Following filter applied");
            }
        }

        BooleanExpression categoryCondition = buildCategoryCondition(request.getCategory());
        if (categoryCondition != null) {
            builder.and(categoryCondition);
            log.debug("Category filter applied: {}", request.getCategory());
        }

        BooleanExpression searchCondition = buildSearchCondition(request.getSearch(), request.getSearchType());
        if (searchCondition != null) {
            builder.and(searchCondition);
            log.debug("Search applied: {} (type: {})", request.getSearch(), request.getSearchType());
        }
        
        return builder;
    }
    
    @Override
    public boolean requiresResourceCountValidation() {
        return true;
    }
    
    @Override
    public String getStrategyName() {
        return "ALL_GAMES";
    }
}
