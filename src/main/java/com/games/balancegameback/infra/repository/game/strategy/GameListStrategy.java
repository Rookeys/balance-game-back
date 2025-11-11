package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.querydsl.core.BooleanBuilder;

/**
 * 게임 리스트 조회 전략 인터페이스
 * 각 타입(PUBLIC, MY_GAMES, FOLLOWING 등)별 필터링 로직을 캡슐화
 */
public interface GameListStrategy {
    
    /**
     * 이 전략의 필터 조건 생성
     * 
     * @param request 검색 요청
     * @param user 현재 사용자 (null 가능)
     * @return QueryDSL BooleanBuilder
     */
    BooleanBuilder buildFilterConditions(GameSearchRequest request, Users user);
    
    /**
     * 리소스 개수 검증이 필요한가?
     * - PUBLIC, FOLLOWING: true
     * - MY_GAMES: false
     * 
     * @return 검증 필요 여부
     */
    boolean requiresResourceCountValidation();
    
    /**
     * 전략 이름
     * 
     * @return 전략 이름
     */
    String getStrategyName();
}
