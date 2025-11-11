package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import org.springframework.util.StringUtils;

/**
 * 전략 공통 로직을 제공하는 추상 클래스
 * 중복되는 필터 조건 생성 로직을 재사용
 */
public abstract class AbstractGameListStrategy implements GameListStrategy {
    
    /**
     * 팔로잉 필터 조건 생성
     */
    protected BooleanExpression buildFollowingCondition(Users user) {
        if (user == null) {
            return null;
        }
        
        return GameQClasses.games.users.uid.in(
            JPAExpressions
                .select(GameQClasses.follow.followingUid)
                .from(GameQClasses.follow)
                .where(GameQClasses.follow.followerUid.eq(user.getUid()))
        );
    }
    
    /**
     * 카테고리 필터 조건 생성
     */
    protected BooleanExpression buildCategoryCondition(com.games.balancegameback.domain.game.enums.Category category) {
        if (category == null) {
            return null;
        }
        return GameQClasses.category.category.eq(category);
    }
    
    /**
     * 제목 검색 조건 생성
     * 게임 제목, 리소스 제목, 작성자 닉네임에서 검색
     */
    protected BooleanExpression buildTitleSearchCondition(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        
        String searchTitle = title.trim();
        return GameQClasses.games.title.containsIgnoreCase(searchTitle)
                .or(GameQClasses.resources.title.containsIgnoreCase(searchTitle))
                .or(GameQClasses.users.nickname.containsIgnoreCase(searchTitle)
                        .and(GameQClasses.games.isNamePrivate.eq(false)));
    }
}
