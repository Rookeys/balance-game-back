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
     * user가 팔로우한 사람들의 게임만 조회
     * 팔로우 관계가 없으면 결과가 없어야 함
     */
    protected BooleanExpression buildFollowingCondition(Users user) {
        if (user == null) {
            return null;
        }
        
        // EXISTS를 사용하여 팔로우 관계가 있는 게임만 조회
        return JPAExpressions
                .selectOne()
                .from(GameQClasses.follow)
                .where(
                    GameQClasses.follow.followerUid.eq(user.getUid())
                        .and(GameQClasses.follow.followingUid.eq(GameQClasses.games.users.uid))
                )
                .exists();
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
