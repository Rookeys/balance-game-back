package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameJpaRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.impl.S3Service;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final GameJpaRepository gameRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final S3Service s3Service;

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
        QGamesEntity games = QGamesEntity.gamesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGameCategoryEntity gameCategory = QGameCategoryEntity.gameCategoryEntity;
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        BooleanBuilder builder = new BooleanBuilder();
        this.setOptions(builder, cursorId, searchRequest, games, resources, gameCategory);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<Tuple> resultTuples = jpaQueryFactory.selectDistinct(
                        games.id,
                        games.title,
                        games.description,
                        games.users.nickname,
                        images.fileUrl,
                        games.createdDate,
                        games.isBlind
                ).from(games)
                .join(games.users).on(games.users.uid.eq(users.getUid()))
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .leftJoin(games.gameResources, resources)
                .leftJoin(games.categories, gameCategory)
                .leftJoin(images).on(images.users.uid.eq(games.users.uid))
                .where(builder)
                .groupBy(games.id, games.title, games.description)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<GameListResponse> resultList = resultTuples.stream().map(tuple -> {
            Long roomId = tuple.get(games.id);
            String title = tuple.get(games.title);
            String description = tuple.get(games.description);
            String nickname = tuple.get(games.users.nickname);
            String profileImageUrl = tuple.get(images.fileUrl);
            OffsetDateTime createdAt = tuple.get(games.createdDate);
            Boolean isBlind = tuple.get(games.isBlind);

            List<Tuple> tuples = jpaQueryFactory.select(
                            resources.id,
                            images.fileUrl.coalesce(links.urls),
                            images.mediaType.coalesce(links.mediaType),
                            links.startSec.coalesce(0),
                            links.endSec.coalesce(0),
                            resources.title
                    ).from(resources)
                    .leftJoin(resources.images, images)
                    .leftJoin(resources.links, links)
                    .where(resources.games.id.eq(roomId))
                    .orderBy(resources.winningLists.size().desc(), resources.id.desc())
                    .offset(0)
                    .limit(2)
                    .fetch();

            // 카테고리 리스트 발급
            List<Category> category = jpaQueryFactory
                    .selectFrom(gameCategory)
                    .where(gameCategory.games.id.eq(roomId))
                    .fetch()
                    .stream()
                    .map(GameCategoryEntity::getCategory)
                    .toList();

            // 전체 플레이 횟수
            Long totalPlayNums = jpaQueryFactory
                    .select(results.id.count())
                    .from(results)
                    .where(results.gameResources.games.id.eq(roomId))
                    .fetchOne();

            // 1주일 플레이 횟수
            Long weekPlayNums = jpaQueryFactory
                    .select(results.id.count())
                    .from(results)
                    .where(results.gameResources.games.id.eq(roomId)
                            .and(results.createdDate.after(OffsetDateTime.now().minusWeeks(1))))
                    .fetchOne();

            GameListSelectionResponse leftSelection = (!tuples.isEmpty()) ?
                    GameListSelectionResponse.builder()
                            .id(tuples.getFirst().get(resources.id))
                            .title(tuples.getFirst().get(resources.title))
                            .type(tuples.getFirst().get(images.mediaType.coalesce(links.mediaType)))
                            .startSec(Optional.ofNullable(tuples.getFirst().get(links.startSec.coalesce(0))).orElse(0))
                            .endSec(Optional.ofNullable(tuples.getFirst().get(links.endSec.coalesce(0))).orElse(0))
                            .content(tuples.getFirst().get(images.fileUrl.coalesce(links.urls)))
                            .build()
                    : null;

            GameListSelectionResponse rightSelection = (!tuples.isEmpty()) ?
                    GameListSelectionResponse.builder()
                            .id(tuples.getLast().get(resources.id))
                            .title(tuples.getLast().get(resources.title))
                            .type(tuples.getLast().get(images.mediaType.coalesce(links.mediaType)))
                            .startSec(Optional.ofNullable(tuples.getLast().get(links.startSec.coalesce(0))).orElse(0))
                            .endSec(Optional.ofNullable(tuples.getLast().get(links.endSec.coalesce(0))).orElse(0))
                            .content(tuples.getLast().get(images.fileUrl.coalesce(links.urls)))
                            .build()
                    : null;

            return GameListResponse.builder()
                    .roomId(roomId)
                    .title(title)
                    .description(description)
                    .categories(category)
                    .existsBlind(isBlind)
                    .createdAt(createdAt)
                    .totalPlayNums(totalPlayNums != null ? totalPlayNums.intValue() : 0)
                    .weekPlayNums(weekPlayNums != null ? weekPlayNums.intValue() : 0)
                    .userResponse(UserMainResponse.builder()
                            .nickname(nickname)
                            .profileImageUrl(profileImageUrl)
                            .build())
                    .leftSelection(leftSelection)
                    .rightSelection(rightSelection)
                    .build();
        }).collect(Collectors.toList());    // toList() 는 불변 리스트로 반환되기 Collectors 로 한번 감싸줘야 함.

        boolean hasNext = resultList.size() > pageable.getPageSize();

        if (hasNext) {
            resultList.removeLast(); // 안전한 마지막 요소 제거
        }

        Long totalElements = (long) jpaQueryFactory
                .selectFrom(games)
                .from(games)
                .leftJoin(games.gameResources, resources)
                .leftJoin(games.categories, gameCategory)
                .where(games.users.uid.eq(users.getUid()))
                .fetch()
                .size();

        return new CustomPageImpl<>(resultList, pageable, totalElements, cursorId, hasNext);
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

    private void setOptions(BooleanBuilder builder, Long cursorId, GameSearchRequest request,
                            QGamesEntity games, QGameResourcesEntity resources, QGameCategoryEntity gameCategory) {
        if (cursorId != null && request.getSortType().equals(GameSortType.OLD)) {
            builder.and(games.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameSortType.RECENT)) {
            builder.and(games.id.lt(cursorId));
        }

        if (request.getCategory() != null) {
            builder.and(gameCategory.category.in(request.getCategory()));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(games.title.containsIgnoreCase(request.getTitle())
                    .or(resources.title.containsIgnoreCase(request.getTitle())));
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(GameSortType sortType) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        // 나중에 조건 추가를 고려해 switch 유지
        return switch (sortType) {
            case OLD -> games.id.asc();
            default -> games.id.desc();
        };
    }
}
