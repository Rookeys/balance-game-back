package com.games.balancegameback.infra.repository.game.service;

import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.infra.repository.game.common.GameBatchData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * GameQueryService 단위 테스트
 * 컴포지션의 장점: 서비스만 독립적으로 테스트 가능
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameQueryService 테스트")
class GameQueryServiceTest {
    
    @Mock
    private JPAQueryFactory jpaQueryFactory;
    
    @Mock
    private CommonGameRepository commonGameRepository;
    
    private GameQueryService gameQueryService;
    
    @BeforeEach
    void setUp() {
        gameQueryService = new GameQueryService(jpaQueryFactory, commonGameRepository);
    }
    
    @Test
    @DisplayName("빈 리스트에 대한 응답 생성")
    void buildGameListResponses_emptyList() {
        // given
        List tuples = Collections.emptyList();
        Users user = null;
        GameSortType sortType = GameSortType.RECENT;
        
        // when
        List<GameListResponse> responses = gameQueryService.buildGameListResponses(tuples, user, sortType);
        
        // then
        assertThat(responses).isEmpty();
    }
    
    @Test
    @DisplayName("정렬 적용 - RECENT (최신순)")
    void applySorting_recent() {
        // given
        List<GameListResponse> responses = createTestResponses();
        
        // when
        List<GameListResponse> sorted = gameQueryService.applySorting(responses, GameSortType.RECENT);
        
        // then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getRoomId()).isEqualTo(3L);  // 최신
        assertThat(sorted.get(1).getRoomId()).isEqualTo(2L);
        assertThat(sorted.get(2).getRoomId()).isEqualTo(1L);  // 오래된
    }
    
    @Test
    @DisplayName("정렬 적용 - OLD (오래된순)")
    void applySorting_old() {
        // given
        List<GameListResponse> responses = createTestResponses();
        
        // when
        List<GameListResponse> sorted = gameQueryService.applySorting(responses, GameSortType.OLD);
        
        // then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getRoomId()).isEqualTo(1L);  // 오래된
        assertThat(sorted.get(1).getRoomId()).isEqualTo(2L);
        assertThat(sorted.get(2).getRoomId()).isEqualTo(3L);  // 최신
    }
    
    @Test
    @DisplayName("정렬 적용 - PLAY_DESC (플레이 많은 순)")
    void applySorting_playDesc() {
        // given
        List<GameListResponse> responses = List.of(
                createResponse(1L, 100),  // 플레이 100회
                createResponse(2L, 500),  // 플레이 500회
                createResponse(3L, 200)   // 플레이 200회
        );
        
        // when
        List<GameListResponse> sorted = gameQueryService.applySorting(responses, GameSortType.PLAY_DESC);
        
        // then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getTotalPlayNums()).isEqualTo(500);
        assertThat(sorted.get(1).getTotalPlayNums()).isEqualTo(200);
        assertThat(sorted.get(2).getTotalPlayNums()).isEqualTo(100);
    }
    
    @Test
    @DisplayName("정렬 적용 - WEEK (주간 플레이 순)")
    void applySorting_week() {
        // given
        List<GameListResponse> responses = List.of(
                GameListResponse.builder()
                        .roomId(1L)
                        .weekPlayNums(10)
                        .build(),
                GameListResponse.builder()
                        .roomId(2L)
                        .weekPlayNums(50)
                        .build(),
                GameListResponse.builder()
                        .roomId(3L)
                        .weekPlayNums(30)
                        .build()
        );
        
        // when
        List<GameListResponse> sorted = gameQueryService.applySorting(responses, GameSortType.WEEK);
        
        // then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getWeekPlayNums()).isEqualTo(50);
        assertThat(sorted.get(1).getWeekPlayNums()).isEqualTo(30);
        assertThat(sorted.get(2).getWeekPlayNums()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("정렬 적용 - MONTH (월간 플레이 순)")
    void applySorting_month() {
        // given
        List<GameListResponse> responses = List.of(
                GameListResponse.builder()
                        .roomId(1L)
                        .monthPlayNums(100)
                        .build(),
                GameListResponse.builder()
                        .roomId(2L)
                        .monthPlayNums(300)
                        .build(),
                GameListResponse.builder()
                        .roomId(3L)
                        .monthPlayNums(200)
                        .build()
        );
        
        // when
        List<GameListResponse> sorted = gameQueryService.applySorting(responses, GameSortType.MONTH);
        
        // then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getMonthPlayNums()).isEqualTo(300);
        assertThat(sorted.get(1).getMonthPlayNums()).isEqualTo(200);
        assertThat(sorted.get(2).getMonthPlayNums()).isEqualTo(100);
    }
    
    // 테스트 헬퍼 메서드
    private List<GameListResponse> createTestResponses() {
        return List.of(
                createResponse(1L, 100),
                createResponse(2L, 200),
                createResponse(3L, 300)
        );
    }
    
    private GameListResponse createResponse(Long roomId, int totalPlayNums) {
        return GameListResponse.builder()
                .roomId(roomId)
                .title("Game " + roomId)
                .totalPlayNums(totalPlayNums)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
