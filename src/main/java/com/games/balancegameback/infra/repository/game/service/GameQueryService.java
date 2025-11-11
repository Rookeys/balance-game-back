package com.games.balancegameback.infra.repository.game.service;

import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameDetailResponse;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.games.balancegameback.infra.repository.game.common.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임 쿼리 실행 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameQueryService {
    
    private final JPAQueryFactory jpaQueryFactory;
    private final CommonGameRepository commonGameRepository;
    
    /**
     * 게임 기본 데이터 조회
     * 
     * @param conditions 필터 조건
     * @param user 현재 사용자 (existsMine 판단용)
     * @param validateResourceCount 리소스 개수 검증 여부
     * @return 게임 기본 정보 Tuple 리스트
     */
    public List<Tuple> fetchGameBasicData(BooleanBuilder conditions, Users user, boolean validateResourceCount) {
        Expression<Boolean> existsMineExpr = buildExistsMineExpression(user);
        
        JPAQuery<Tuple> query = jpaQueryFactory
                .select(
                    GameQClasses.games.id,                      // 0
                    GameQClasses.games.title,                   // 1
                    GameQClasses.games.description,             // 2
                    GameQClasses.games.users.nickname,          // 3
                    GameQClasses.images.fileUrl.max(),          // 4
                    GameQClasses.games.isNamePrivate,           // 5
                    GameQClasses.games.createdDate,             // 6
                    GameQClasses.games.isBlind,                 // 7
                    existsMineExpr                              // 8
                )
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.images).on(GameQClasses.images.users.uid.eq(GameQClasses.users.uid))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.games.categories, GameQClasses.category)
                .where(conditions)
                .groupBy(GameQClasses.games.id);
        
        // 전략에 따라 리소스 개수 검증 추가
        if (validateResourceCount) {
            query.having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT));
        }
        
        List<Tuple> result = query.fetch();
        log.debug("Fetched {} game basic data (validateResourceCount: {})", result.size(), validateResourceCount);
        return result;
    }
    
    /**
     * 게임 상세 데이터 조회
     * 
     * @param gameId 게임 ID
     * @param user 현재 사용자
     * @return 게임 상세 정보 Tuple
     */
    public Tuple fetchGameDetailData(Long gameId, Users user) {
        Expression<Boolean> existsMineExpr = buildExistsMineExpression(user);
        
        Tuple result = jpaQueryFactory
                .select(
                    GameQClasses.games.id,
                    GameQClasses.games.title,
                    GameQClasses.games.description,
                    GameQClasses.games.users.nickname,
                    GameQClasses.games.isNamePrivate,
                    GameQClasses.games.createdDate,
                    GameQClasses.games.updatedDate,
                    GameQClasses.games.isBlind,
                    GameQClasses.images.fileUrl.max(),
                    existsMineExpr,
                    GameQClasses.results.count().coalesce(GameConstants.DEFAULT_COUNT).as("totalPlays"),
                    GameQClasses.resources.count().coalesce(GameConstants.DEFAULT_COUNT).as("totalResources")
                )
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.images).on(GameQClasses.images.users.uid.eq(GameQClasses.users.uid))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.eq(gameId))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
        
        log.debug("Fetched game detail data for gameId: {}", gameId);
        return result;
    }
    
    /**
     * Tuple 리스트를 GameListResponse로 변환
     * 
     * @param tuples 게임 기본 데이터
     * @param user 현재 사용자
     * @param sortType 정렬 타입
     * @return GameListResponse 리스트
     */
    public List<GameListResponse> buildGameListResponses(List<Tuple> tuples, Users user, GameSortType sortType) {
        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> gameIds = commonGameRepository.extractGameIds(tuples);
        GameBatchData batchData = commonGameRepository.getAllBatchData(gameIds, sortType);
        
        List<GameListResponse> responses = tuples.stream()
                .map(tuple -> commonGameRepository.buildGameListResponse(tuple, user, batchData))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.debug("Built {} GameListResponses from {} tuples", responses.size(), tuples.size());
        return responses;
    }
    
    /**
     * GameDetailResponse 생성
     * 
     * @param gameData 게임 기본 데이터
     * @param categories 카테고리 목록
     * @param selections 상위 선택지
     * @param user 현재 사용자
     * @return GameDetailResponse
     */
    public GameDetailResponse buildGameDetailResponse(Tuple gameData, List<Category> categories,
                                                       List<GameListSelectionResponse> selections, Users user) {
        String nickname = gameData.get(GameQClasses.games.users.nickname);
        String profileImageUrl = gameData.get(8, String.class);
        boolean isPrivate = Boolean.TRUE.equals(gameData.get(GameQClasses.games.isNamePrivate));
        
        if (isPrivate) {
            nickname = GameConstants.ANONYMOUS_NICKNAME;
            profileImageUrl = null;
        }
        
        return GameDetailResponse.builder()
                .title(gameData.get(GameQClasses.games.title))
                .description(gameData.get(GameQClasses.games.description))
                .categories(categories)
                .existsBlind(gameData.get(GameQClasses.games.isBlind))
                .existsMine(Boolean.TRUE.equals(gameData.get(9, Boolean.class)))
                .totalPlayNums(commonGameRepository.safeIntValue(gameData.get(10, Long.class)))
                .totalResourceNums(commonGameRepository.safeIntValue(gameData.get(11, Long.class)))
                .createdAt(gameData.get(GameQClasses.games.createdDate))
                .updatedAt(gameData.get(GameQClasses.games.updatedDate))
                .userResponse(UserMainResponse.builder()
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .build())
                .leftSelection(!selections.isEmpty() ? selections.get(0) : null)
                .rightSelection(selections.size() > 1 ? selections.get(1) : null)
                .build();
    }
    
    /**
     * 정렬 적용
     * 
     * @param responses 응답 리스트
     * @param sortType 정렬 타입
     * @return 정렬된 응답 리스트
     */
    public List<GameListResponse> applySorting(List<GameListResponse> responses, GameSortType sortType) {
        List<GameListResponse> sorted = switch (sortType) {
            case OLD -> responses.stream()
                    .sorted(Comparator.comparing(GameListResponse::getRoomId))
                    .collect(Collectors.toList());
            case RECENT -> responses.stream()
                    .sorted(Comparator.comparing(GameListResponse::getRoomId).reversed())
                    .collect(Collectors.toList());
            case WEEK -> responses.stream()
                    .sorted((r1, r2) -> {
                        int weekCompare = Integer.compare(r2.getWeekPlayNums(), r1.getWeekPlayNums());
                        return weekCompare != 0 ? weekCompare : Long.compare(r2.getRoomId(), r1.getRoomId());
                    })
                    .collect(Collectors.toList());
            case MONTH -> responses.stream()
                    .sorted(Comparator.comparingInt(GameListResponse::getMonthPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
            case PLAY_DESC -> responses.stream()
                    .sorted(Comparator.comparingInt(GameListResponse::getTotalPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
        };
        
        log.debug("Applied sorting: {} on {} items", sortType, responses.size());
        return sorted;
    }
    
    /**
     * 총 게임 개수 계산
     * @param conditions 필터 조건
     * @param validateResourceCount 리소스 개수 검증 여부
     * @return 총 개수
     */
    public Long calculateTotalElements(BooleanBuilder conditions, boolean validateResourceCount) {
        JPAQuery<?> query = jpaQueryFactory
                .selectFrom(GameQClasses.games)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.games.eq(GameQClasses.games))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.games.categories, GameQClasses.category)
                .leftJoin(GameQClasses.games.users, GameQClasses.users)
                .where(conditions)
                .groupBy(GameQClasses.games.id);
        
        if (validateResourceCount) {
            query.having(GameQClasses.games.gameResources.size().goe(GameConstants.MIN_RESOURCE_COUNT));
        }
        
        long total = query.fetch().size();
        log.debug("Calculated total elements: {} (validateResourceCount: {})", total, validateResourceCount);
        return total;
    }
    
    /**
     * existsMine 표현식 생성
     */
    private Expression<Boolean> buildExistsMineExpression(Users user) {
        return user != null ?
                GameQClasses.games.users.uid.eq(user.getUid()) :
                Expressions.FALSE;
    }
}
