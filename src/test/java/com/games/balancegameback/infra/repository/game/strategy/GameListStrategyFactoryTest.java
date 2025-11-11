package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.game.enums.GameListType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GameListStrategyFactory 단위 테스트
 * 전략 패턴의 핵심: 팩토리가 올바른 전략을 반환하는지 확인
 */
@DisplayName("GameListStrategyFactory 테스트")
class GameListStrategyFactoryTest {
    
    private GameListStrategyFactory factory;
    private PublicGamesStrategy publicStrategy;
    private MyGamesStrategy myGamesStrategy;
    private AllGamesStrategy allGamesStrategy;
    
    @BeforeEach
    void setUp() {
        publicStrategy = new PublicGamesStrategy();
        myGamesStrategy = new MyGamesStrategy();
        allGamesStrategy = new AllGamesStrategy();
        
        factory = new GameListStrategyFactory(publicStrategy, myGamesStrategy, allGamesStrategy);
    }
    
    @Test
    @DisplayName("PUBLIC 타입에 대한 전략 반환")
    void getStrategy_public() {
        // given
        GameListType type = GameListType.PUBLIC;
        
        // when
        GameListStrategy strategy = factory.getStrategy(type);
        
        // then
        assertThat(strategy).isNotNull();
        assertThat(strategy).isInstanceOf(PublicGamesStrategy.class);
        assertThat(strategy.getStrategyName()).isEqualTo("PUBLIC_GAMES");
    }
    
    @Test
    @DisplayName("MY_GAMES 타입에 대한 전략 반환")
    void getStrategy_myGames() {
        // given
        GameListType type = GameListType.MY_GAMES;
        
        // when
        GameListStrategy strategy = factory.getStrategy(type);
        
        // then
        assertThat(strategy).isNotNull();
        assertThat(strategy).isInstanceOf(MyGamesStrategy.class);
        assertThat(strategy.getStrategyName()).isEqualTo("MY_GAMES");
    }
    
    @Test
    @DisplayName("ALL 타입에 대한 전략 반환")
    void getStrategy_all() {
        // given
        GameListType type = GameListType.ALL;
        
        // when
        GameListStrategy strategy = factory.getStrategy(type);
        
        // then
        assertThat(strategy).isNotNull();
        assertThat(strategy).isInstanceOf(AllGamesStrategy.class);
        assertThat(strategy.getStrategyName()).isEqualTo("ALL_GAMES");
    }
    
    @Test
    @DisplayName("null 타입은 예외 발생")
    void getStrategy_null() {
        // given
        GameListType type = null;
        
        // when & then
        assertThatThrownBy(() -> factory.getStrategy(type))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown GameListType");
    }
    
    @Test
    @DisplayName("여러 번 호출해도 같은 인스턴스 반환 (싱글톤)")
    void getStrategy_singleton() {
        // given
        GameListType type = GameListType.PUBLIC;
        
        // when
        GameListStrategy strategy1 = factory.getStrategy(type);
        GameListStrategy strategy2 = factory.getStrategy(type);
        
        // then
        assertThat(strategy1).isSameAs(strategy2);  // 같은 인스턴스
    }
}
