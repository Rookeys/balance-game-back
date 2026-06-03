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
 * PUBLIC 게임 목록 조회 전략
 * - accessType이 PUBLIC인 게임만 조회
 * - 카테고리, 제목, 팔로잉 필터 모두 적용 가능
 * - 최소 2개 이상의 리소스 필요
 */
@Slf4j
@Component
public class PublicGamesStrategy extends AbstractGameListStrategy {
    
    @Override
    public BooleanBuilder buildFilterConditions(GameSearchRequest request, Users user) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // 1. PUBLIC 게임만
        builder.and(GameQClasses.games.accessType.eq(AccessType.PUBLIC));
        
        // 2. 팔로잉 필터 (선택적)
        if (Boolean.TRUE.equals(request.getFollowingOnly())) {
            BooleanExpression followingCondition = buildFollowingCondition(user);
            if (followingCondition != null) {
                builder.and(followingCondition);
                log.debug("Following filter applied for user: {}", user != null ? user.getUid() : "null");
            }
        }
        
        // 3. 카테고리 필터 (선택적)
        BooleanExpression categoryCondition = buildCategoryCondition(request.getCategory());
        if (categoryCondition != null) {
            builder.and(categoryCondition);
            log.debug("Category filter applied: {}", request.getCategory());
        }
        
        // 4. 검색어 필터 (선택적)
        BooleanExpression searchCondition = buildSearchCondition(request.getSearch(), request.getSearchType());
        if (searchCondition != null) {
            builder.and(searchCondition);
            log.debug("Search applied: {} (type: {})", request.getSearch(), request.getSearchType());
        }
        
        return builder;
    }
    
    @Override
    public boolean requiresResourceCountValidation() {
        return true;  // PUBLIC 게임은 최소 2개 리소스 필요
    }
    
    @Override
    public String getStrategyName() {
        return "PUBLIC_GAMES";
    }
}
