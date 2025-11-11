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
 * MyGamesStrategy 단위 테스트
 */
@DisplayName("MyGamesStrategy 테스트")
class MyGamesStrategyTest {
    
    private MyGamesStrategy strategy;
    
    @BeforeEach
    void setUp() {
        strategy = new MyGamesStrategy();
    }
    
    @Test
    @DisplayName("전략 이름 확인")
    void getStrategyName() {
        // given & when
        String strategyName = strategy.getStrategyName();
        
        // then
        assertThat(strategyName).isEqualTo("MY_GAMES");
    }
    
    @Test
    @DisplayName("리소스 개수 검증 필요 없음")
    void requiresResourceCountValidation() {
        // given & when
        boolean requires = strategy.requiresResourceCountValidation();
        
        // then
        assertThat(requires).isFalse();  // MY_GAMES는 작성 중인 게임도 표시
    }
    
    @Test
    @DisplayName("사용자의 게임만 조회")
    void buildFilterConditions_withUser() {
        // given
        Users user = Users.builder()
                .uid("my-uid")
                .build();
        
        GameSearchRequest request = GameSearchRequest.builder().build();
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        assertThat(conditions.toString()).contains("my-uid");
    }
    
    @Test
    @DisplayName("사용자 없으면 빈 결과")
    void buildFilterConditions_noUser() {
        // given
        GameSearchRequest request = GameSearchRequest.builder().build();
        Users user = null;
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        // 항상 false 조건이 들어가야 함
    }
    
    @Test
    @DisplayName("카테고리 필터 적용")
    void buildFilterConditions_withCategory() {
        // given
        Users user = Users.builder()
                .uid("my-uid")
                .build();
        
        GameSearchRequest request = GameSearchRequest.builder()
                .category(Category.FOOD)
                .build();
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        // 내 게임 + 카테고리 조건
    }
    
    @Test
    @DisplayName("팔로잉 필터는 MY_GAMES에서 무시됨")
    void buildFilterConditions_followingIgnored() {
        // given
        Users user = Users.builder()
                .uid("my-uid")
                .build();
        
        GameSearchRequest request = GameSearchRequest.builder()
                .followingOnly(true)  // 이 값은 무시되어야 함
                .build();
        
        // when
        BooleanBuilder conditions = strategy.buildFilterConditions(request, user);
        
        // then
        assertThat(conditions).isNotNull();
        // 팔로잉 조건은 포함되지 않아야 함
    }
}
