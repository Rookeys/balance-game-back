package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PublicGamesStrategy 단위 테스트
 * 전략 패턴의 장점: 각 전략을 독립적으로 테스트 가능
 */
@DisplayName("PublicGamesStrategy 테스트")
class PublicGamesStrategyTest {
    
    private PublicGamesStrategy strategy;
    
    @BeforeEach
    void setUp() {
        strategy = new PublicGamesStrategy();
    }
    
    @Test
    @DisplayName("전략 이름 확인")
    void getStrategyName() {
        // given & when
        String strategyName = strategy.getStrategyName();
        
        // then
        assertThat(strategyName).isEqualTo("PUBLIC_GAMES");
    }
    
    @Test
    @DisplayName("리소스 개수 검증 필요 여부 확인")
    void requiresResourceCountValidation() {
        // given & when
        boolean requires = strategy.requiresResourceCountValidation();
        
        // then
        assertThat(requires).isTrue();
    }
    
    @Test
    @DisplayName("기본 조건만 있는 경우")
    void buildFilterConditions_basicOnly() {
        // given
        GameSearchRequest request = GameSearchRequest.builder().build();
        Users user = null;
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("accessType = PUBLIC");
    }
    
    @Test
    @DisplayName("카테고리 필터 적용")
    void buildFilterConditions_withCategory() {
        // given
        GameSearchRequest request = GameSearchRequest.builder()
                .category(Category.FOOD)
                .build();
        Users user = null;
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("accessType = PUBLIC");
    }
    
    @Test
    @DisplayName("제목 검색 필터 적용")
    void buildFilterConditions_withTitle() {
        // given
        GameSearchRequest request = GameSearchRequest.builder()
                .title("테스트")
                .build();
        Users user = null;
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("accessType = PUBLIC");
    }
    
    @Test
    @DisplayName("팔로잉 필터 적용 - 사용자 있을 때")
    void buildFilterConditions_withFollowingOnly() {
        // given
        Users user = Users.builder()
                .uid("test-uid")
                .build();
        
        GameSearchRequest request = GameSearchRequest.builder()
                .followingOnly(true)
                .build();
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("accessType = PUBLIC");
    }
    
    @Test
    @DisplayName("팔로잉 필터 적용 - 사용자 없을 때는 무시")
    void buildFilterConditions_withFollowingOnly_noUser() {
        // given
        GameSearchRequest request = GameSearchRequest.builder()
                .followingOnly(true)
                .build();
        Users user = null;
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("accessType = PUBLIC");
    }
    
    @Test
    @DisplayName("모든 필터 조합")
    void buildFilterConditions_allFilters() {
        // given
        Users user = Users.builder()
                .uid("test-uid")
                .build();
        
        GameSearchRequest request = GameSearchRequest.builder()
                .category(Category.SONG)
                .title("아이돌")
                .followingOnly(true)
                .build();
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        String conditionString = conditions.toString();
        assertThat(conditionString).contains("accessType = PUBLIC");
    }
}
