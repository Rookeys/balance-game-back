package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameJpaRepository;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.infra.repository.game.service.GameQueryService;
import com.games.balancegameback.infra.repository.game.strategy.GameListStrategy;
import com.games.balancegameback.infra.repository.game.strategy.GameListStrategyFactory;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final GameJpaRepository gameRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final GameListStrategyFactory strategyFactory;
    private final GameQueryService gameQueryService;
    private final CommonGameRepository commonGameRepository;

    @Override
    public Games save(Games games) {
        GamesEntity entity = gameRepository.save(GamesEntity.from(games));
        return entity.toModel();
    }

    @Override
    public GameResponse findById(Long roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        return GameResponse.builder()
                .roomId(roomId)
                .title(gamesEntity.getTitle())
                .description(gamesEntity.getDescription())
                .existsNamePrivate(gamesEntity.getIsNamePrivate())
                .existsBlind(gamesEntity.getIsBlind())
                .accessType(gamesEntity.getAccessType())
                .inviteCode(gamesEntity.getGameInviteCode().getInviteCode())
                .categories(gamesEntity.getCategories().stream()
                        .map(GameCategoryEntity::toModel)
                        .map(GameCategory::getCategory)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Games findByRoomId(Long roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        return gamesEntity.toModel();
    }

    @Override
    public CustomPageImpl<GameListResponse> findGamesWithResources(Long cursorId, Users users,
                                                                   Pageable pageable,
                                                                   GameSearchRequest searchRequest) {
        try {
            log.info("Getting MY_GAMES list - cursorId: {}, pageSize: {}, user: {}", 
                     cursorId, pageable.getPageSize(), users != null ? users.getUid() : "null");
            
            // MY_GAMES 전략 사용
            GameListStrategy strategy = strategyFactory.getStrategy(GameListType.MY_GAMES);
            
            // 필터 조건 생성
            BooleanBuilder conditions = strategy.buildFilterConditions(searchRequest, users);
            
            // 기본 데이터 조회
            List<Tuple> baseTuples = gameQueryService.fetchGameBasicData(
                conditions,
                users,
                strategy.requiresResourceCountValidation()
            );
            
            if (baseTuples.isEmpty()) {
                log.info("No MY_GAMES found for user: {}", users != null ? users.getUid() : "null");
                return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
            }
            
            // 응답 생성
            List<GameListResponse> responses = gameQueryService.buildGameListResponses(
                baseTuples,
                users,
                searchRequest.getSortType()
            );
            
            // 정렬
            List<GameListResponse> sortedResponses = gameQueryService.applySorting(
                responses,
                searchRequest.getSortType()
            );
            
            // 페이징
            List<GameListResponse> pagedResponses = commonGameRepository.applyCursorPagingWithCustomCursor(
                sortedResponses,
                cursorId,
                GameListResponse::getRoomId,
                pageable
            );
            
            boolean hasNext = pagedResponses.size() > pageable.getPageSize();
            if (hasNext) {
                pagedResponses.removeLast();
            }
            
            // 총 개수 계산
            Long totalElements = gameQueryService.calculateTotalElements(
                conditions,
                strategy.requiresResourceCountValidation()
            );
            
            log.info("MY_GAMES list retrieved - total: {}, returned: {}, hasNext: {}", 
                     totalElements, pagedResponses.size(), hasNext);
            
            return new CustomPageImpl<>(pagedResponses, pageable, totalElements, cursorId, hasNext);
            
        } catch (Exception e) {
            log.error("Error in findGamesWithResources", e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    @Override
    public boolean existsIdAndUsers(Long gameId, Users users) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        BooleanExpression condition = games.id.eq(gameId)
                .and(games.users.uid.eq(users.getUid()));

        Integer result = jpaQueryFactory
                .selectOne()
                .from(games)
                .where(condition)
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsGameRounds(Long gameId, int roundNumber) {
        GamesEntity games = gameRepository.findById(gameId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        return games.getGameResources().size() >= roundNumber;
    }

    @Override
    public void update(Games games) {
        GamesEntity gamesEntity = gameRepository.findById(games.getId()).orElseThrow();
        gamesEntity.update(games);
    }
}
