package com.games.balancegameback.core.utils;

import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.repository.game.common.AbstractGameRepository;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameRepositoryFactory {

    private final JPAQueryFactory jpaQueryFactory;
    private final CommonGameRepository commonGameRepository;

    /**
     * 공개 게임 리스트 조회
     */
    public CustomPageImpl<GameListResponse> getPublicGameList(Long cursorId, Pageable pageable,
                                                              GameSearchRequest searchRequest, Users user) {
        return new GameListExecutor(jpaQueryFactory, commonGameRepository)
                .execute(cursorId, pageable, searchRequest, user, GameListType.PUBLIC);
    }

    /**
     * 내 게임 리스트 조회
     */
    public CustomPageImpl<GameListResponse> getMyGameList(Long cursorId, Pageable pageable,
                                                          GameSearchRequest searchRequest, Users user) {
        return new GameListExecutor(jpaQueryFactory, commonGameRepository)
                .execute(cursorId, pageable, searchRequest, user, GameListType.MY_GAMES);
    }

    /**
     * 접근 가능한 모든 게임 리스트 조회
     */
    public CustomPageImpl<GameListResponse> getAllAccessibleGameList(Long cursorId, Pageable pageable,
                                                                     GameSearchRequest searchRequest, Users user) {
        return new GameListExecutor(jpaQueryFactory, commonGameRepository)
                .execute(cursorId, pageable, searchRequest, user, GameListType.ALL);
    }

    /**
     * 카테고리 개수 조회
     */
    public GameCategoryNumsResponse getCategoryCounts(String title, GameListType listType, Users user) {
        return new CategoryCountExecutor(jpaQueryFactory, commonGameRepository)
                .execute(title, listType, user);
    }

    /**
     * 게임 상세 정보 조회
     */
    public GameDetailResponse getGameDetail(Long gameId, Users user) {
        return new GameDetailExecutor(jpaQueryFactory, commonGameRepository)
                .execute(gameId, user);
    }

    /**
     * 게임 결과 랭킹 조회
     */
    public CustomPageImpl<GameResultResponse> getGameResultRanking(Long gameId, Long cursorId,
                                                                   GameResourceSearchRequest request, Pageable pageable) {
        return new GameResultExecutor(jpaQueryFactory, commonGameRepository)
                .execute(gameId, cursorId, request, pageable);
    }

    // =========================== 내부 실행 클래스들 ===========================

    private static class GameListExecutor extends AbstractGameRepository {
        public GameListExecutor(JPAQueryFactory jpaQueryFactory, CommonGameRepository commonGameRepository) {
            super(jpaQueryFactory, commonGameRepository);
        }

        public CustomPageImpl<GameListResponse> execute(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest, Users user, GameListType listType) {
            return findGameListAutomated(cursorId, pageable, searchRequest, user, listType);
        }
    }

    private static class CategoryCountExecutor extends AbstractGameRepository {
        public CategoryCountExecutor(JPAQueryFactory jpaQueryFactory, CommonGameRepository commonGameRepository) {
            super(jpaQueryFactory, commonGameRepository);
        }

        public GameCategoryNumsResponse execute(String title, GameListType listType, Users user) {
            return getCategoryCountsAutomated(title, listType, user);
        }
    }

    private static class GameDetailExecutor extends AbstractGameRepository {
        public GameDetailExecutor(JPAQueryFactory jpaQueryFactory, CommonGameRepository commonGameRepository) {
            super(jpaQueryFactory, commonGameRepository);
        }

        public GameDetailResponse execute(Long gameId, Users user) {
            return getGameDetailAutomated(gameId, user);
        }
    }

    private record GameResultExecutor(JPAQueryFactory jpaQueryFactory, CommonGameRepository commonGameRepository) {
        public CustomPageImpl<GameResultResponse> execute(Long gameId, Long cursorId,
                                                              GameResourceSearchRequest request, Pageable pageable) {

            return new CustomPageImpl<>(java.util.Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }
}
