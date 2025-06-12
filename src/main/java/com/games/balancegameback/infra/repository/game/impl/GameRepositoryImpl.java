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
import com.games.balancegameback.infra.repository.game.GameCategoryJpaRepository;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.infra.repository.user.UserJpaRepository;
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
    private final GameCategoryJpaRepository gameCategoryRepository;
    private final GameResourceJpaRepository gameResourceRepository;
    private final UserJpaRepository userRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final S3Service s3Service;

    @Override
    public Games save(Games games) {
        GamesEntity entity = gameRepository.save(GamesEntity.from(games));

        // 사용자 정보 조회
        UsersEntity userEntity = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        Games savedGames = entity.toModel();
        savedGames.setUsers(userEntity.toModel());
        return savedGames;
    }

    @Override
    public GameResponse findById(String roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        // 카테고리 조회
        List<Category> categories = gameCategoryRepository.findByGameId(roomId).stream()
                .map(GameCategoryEntity::getCategory)
                .collect(Collectors.toList());

        return GameResponse.builder()
                .roomId(roomId)
                .title(gamesEntity.getTitle())
                .description(gamesEntity.getDescription())
                .existsNamePrivate(gamesEntity.getIsNamePrivate())
                .existsBlind(gamesEntity.getIsBlind())
                .accessType(gamesEntity.getAccessType())
                .inviteCode(gamesEntity.getAccessType().name().equals("PROTECTED") ? roomId : null)
                .categories(categories)
                .build();
    }

    @Override
    public Games findByRoomId(String roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        // 사용자 정보 조회
        UsersEntity userEntity = userRepository.findById(gamesEntity.getUserId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        Games games = gamesEntity.toModel();
        games.setUsers(userEntity.toModel());
        return games;
    }

    @Override
    public CustomPageImpl<GameListResponse> findGamesWithResources(String cursorId, Users users,
                                                                   Pageable pageable,
                                                                   GameSearchRequest searchRequest) {
        QGamesEntity games = QGamesEntity.gamesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGameCategoryEntity gameCategory = QGameCategoryEntity.gameCategoryEntity;
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;
        QUsersEntity qUser = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(games.userId.eq(users.getUid()));

        this.setOptions(builder, cursorId, searchRequest, games, resources, gameCategory);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<Tuple> resultTuples = jpaQueryFactory.selectDistinct(
                        games.id,
                        games.title,
                        games.description,
                        qUser.nickname,
                        images.fileUrl,
                        games.createdDate,
                        games.isBlind
                ).from(games)
                .join(qUser).on(games.userId.eq(qUser.id))
                .leftJoin(results).on(results.gameResourceId.in(
                        jpaQueryFactory.select(resources.id)
                                .from(resources)
                                .where(resources.gameId.eq(games.id))
                ))
                .leftJoin(resources).on(resources.gameId.eq(games.id))
                .leftJoin(gameCategory).on(gameCategory.gameId.eq(games.id))
                .leftJoin(images).on(images.userId.eq(games.userId))
                .where(builder)
                .groupBy(games.id, games.title, games.description)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<GameListResponse> resultList = resultTuples.stream().map(tuple -> {
            String roomId = tuple.get(games.id);
            String title = tuple.get(games.title);
            String description = tuple.get(games.description);
            String nickname = tuple.get(qUser.nickname);
            String profileImageUrl = tuple.get(images.fileUrl);
            OffsetDateTime createdAt = tuple.get(games.createdDate);
            Boolean isBlind = tuple.get(games.isBlind);

            // 리소스 정보 조회
            List<Tuple> resourceTuples = jpaQueryFactory.select(
                            resources.id,
                            images.fileUrl.coalesce(links.urls),
                            images.mediaType.coalesce(links.mediaType),
                            links.startSec.coalesce(0),
                            links.endSec.coalesce(0),
                            resources.title
                    ).from(resources)
                    .leftJoin(images).on(images.id.eq(resources.imageId))
                    .leftJoin(links).on(links.id.eq(resources.linkId))
                    .where(resources.gameId.eq(roomId))
                    .orderBy(resources.createdDate.desc())
                    .limit(2)
                    .fetch();

            // 카테고리 리스트 발급
            List<Category> category = jpaQueryFactory
                    .selectFrom(gameCategory)
                    .where(gameCategory.gameId.eq(roomId))
                    .fetch()
                    .stream()
                    .map(GameCategoryEntity::getCategory)
                    .toList();

            // 전체 플레이 횟수
            Long totalPlayNums = jpaQueryFactory
                    .select(results.id.count())
                    .from(results)
                    .join(resources).on(results.gameResourceId.eq(resources.id))
                    .where(resources.gameId.eq(roomId))
                    .fetchOne();

            // 1주일 플레이 횟수
            Long weekPlayNums = jpaQueryFactory
                    .select(results.id.count())
                    .from(results)
                    .join(resources).on(results.gameResourceId.eq(resources.id))
                    .where(resources.gameId.eq(roomId)
                            .and(results.createdDate.after(OffsetDateTime.now().minusWeeks(1))))
                    .fetchOne();

            GameListSelectionResponse leftSelection = (!resourceTuples.isEmpty()) ?
                    GameListSelectionResponse.builder()
                            .id(resourceTuples.get(0).get(resources.id))
                            .title(resourceTuples.get(0).get(resources.title))
                            .type(resourceTuples.get(0).get(images.mediaType.coalesce(links.mediaType)))
                            .startSec(Optional.ofNullable(resourceTuples.get(0).get(links.startSec.coalesce(0))).orElse(0))
                            .endSec(Optional.ofNullable(resourceTuples.get(0).get(links.endSec.coalesce(0))).orElse(0))
                            .content(resourceTuples.get(0).get(images.fileUrl.coalesce(links.urls)))
                            .build()
                    : null;

            GameListSelectionResponse rightSelection = (resourceTuples.size() > 1) ?
                    GameListSelectionResponse.builder()
                            .id(resourceTuples.get(1).get(resources.id))
                            .title(resourceTuples.get(1).get(resources.title))
                            .type(resourceTuples.get(1).get(images.mediaType.coalesce(links.mediaType)))
                            .startSec(Optional.ofNullable(resourceTuples.get(1).get(links.startSec.coalesce(0))).orElse(0))
                            .endSec(Optional.ofNullable(resourceTuples.get(1).get(links.endSec.coalesce(0))).orElse(0))
                            .content(resourceTuples.get(1).get(images.fileUrl.coalesce(links.urls)))
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
        }).collect(Collectors.toList());

        boolean hasNext = resultList.size() > pageable.getPageSize();

        if (hasNext) {
            resultList.remove(resultList.size() - 1);
        }

        Long totalElements = jpaQueryFactory
                .select(games.id.countDistinct())
                .from(games)
                .leftJoin(resources).on(resources.gameId.eq(games.id))
                .leftJoin(gameCategory).on(gameCategory.gameId.eq(games.id))
                .where(games.userId.eq(users.getUid()))
                .fetchOne();

        return new CustomPageImpl<>(resultList, pageable, totalElements != null ? totalElements : 0L, cursorId, hasNext);
    }

    @Override
    public boolean existsIdAndUsers(String gameId, Users users) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        BooleanExpression condition = games.id.eq(gameId)
                .and(games.userId.eq(users.getUid()));

        Integer result = jpaQueryFactory
                .selectOne()
                .from(games)
                .where(condition)
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsGameRounds(String gameId, int roundNumber) {
        GamesEntity games = gameRepository.findById(gameId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        Long resourceCount = gameResourceRepository.countByGameId(gameId);
        return resourceCount >= roundNumber;
    }

    @Override
    public void update(Games games) {
        GamesEntity gamesEntity = gameRepository.findById(games.getId()).orElseThrow();
        gamesEntity.update(games);
        gameRepository.save(gamesEntity);
    }

    @Override
    public void deleteById(String roomId) {
        gameRepository.deleteById(roomId);
    }

    @Override
    public void deleteImagesInS3(String roomId) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;

        List<String> imageUrls = jpaQueryFactory
                .select(images.fileUrl)
                .from(resources)
                .join(images).on(images.id.eq(resources.imageId))
                .where(resources.gameId.eq(roomId))
                .fetch();

        s3Service.deleteImagesAsync(imageUrls);
    }

    private void setOptions(BooleanBuilder builder, String cursorId, GameSearchRequest request,
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

    private OrderSpecifier<?> getOrderSpecifier(GameSortType sortType) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        return switch (sortType) {
            case OLD -> games.id.asc();
            default -> games.id.desc();
        };
    }
}