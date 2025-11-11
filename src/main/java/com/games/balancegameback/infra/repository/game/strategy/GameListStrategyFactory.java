package com.games.balancegameback.infra.repository.game.strategy;

import com.games.balancegameback.domain.game.enums.GameListType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 게임 리스트 전략 팩토리
 * GameListType에 따라 적절한 전략을 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameListStrategyFactory {
    
    private final PublicGamesStrategy publicGamesStrategy;
    private final MyGamesStrategy myGamesStrategy;
    private final AllGamesStrategy allGamesStrategy;
    
    private Map<GameListType, GameListStrategy> strategies;
    
    /**
     * 전략 맵 초기화 (지연 초기화)
     */
    private Map<GameListType, GameListStrategy> getStrategies() {
        if (strategies == null) {
            strategies = new EnumMap<>(GameListType.class);
            strategies.put(GameListType.PUBLIC, publicGamesStrategy);
            strategies.put(GameListType.MY_GAMES, myGamesStrategy);
            strategies.put(GameListType.ALL, allGamesStrategy);
            log.info("GameListStrategy factory initialized with {} strategies", strategies.size());
        }
        return strategies;
    }
    
    /**
     * GameListType에 해당하는 전략 반환
     * 
     * @param type 게임 리스트 타입
     * @return 해당하는 전략
     * @throws IllegalArgumentException 지원하지 않는 타입인 경우
     */
    public GameListStrategy getStrategy(GameListType type) {
        GameListStrategy strategy = getStrategies().get(type);
        
        if (strategy == null) {
            log.error("Unknown GameListType: {}", type);
            throw new IllegalArgumentException("Unknown GameListType: " + type);
        }
        
        log.debug("Strategy selected: {} for type: {}", strategy.getStrategyName(), type);
        return strategy;
    }
}
