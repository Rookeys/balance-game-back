package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MY_GAMES 게임 목록 조회 전략
 * - 내가 만든 게임만 조회
 * - 카테고리, 제목 필터 적용 가능
 * - 리소스 개수 제한 없음
 */
@Slf4j
@Component
public class MyGamesStrategy extends AbstractGameListStrategy {
    
    @Override
    public BooleanBuilder buildFilterConditions(GameSearchRequest request, Users user) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // 내가 만든 게임만
        if (user != null) {
            builder.and(GameQClasses.games.users.uid.eq(user.getUid()));
            log.debug("My games filter applied for user: {}", user.getUid());
        } else {
            // 로그인하지 않은 경우 빈 결과 반환
            log.warn("My games requested without user login");
            builder.and(GameQClasses.games.id.isNull());
        }

        BooleanExpression categoryCondition = buildCategoryCondition(request.getCategory());
        if (categoryCondition != null) {
            builder.and(categoryCondition);
            log.debug("Category filter applied: {}", request.getCategory());
        }

        // 내 게임에서는 게임 제목만 검색 (searchType 무관)
        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            String keyword = request.getSearch().trim();
            builder.and(GameQClasses.games.title.containsIgnoreCase(keyword));
            log.debug("Search applied: {}", keyword);
        }
        
        return builder;
    }
    
    @Override
    public boolean requiresResourceCountValidation() {
        return false;  // 내 게임은 리소스 개수 제한 없음
    }
    
    @Override
    public String getStrategyName() {
        return "MY_GAMES";
    }
}
