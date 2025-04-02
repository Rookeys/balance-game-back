package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.dto.game.GameCategoryNumsResponse;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GameListRepositoryImpl implements GameListRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public GameCategoryNumsResponse getCategoryCounts(String title) {
        QGamesEntity games = QGamesEntity.gamesEntity;
        QGameCategoryEntity category = QGameCategoryEntity.gameCategoryEntity;
        QUsersEntity users = QUsersEntity.usersEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        Map<Category, Long> counts = new EnumMap<>(Category.class);
        BooleanBuilder builder = new BooleanBuilder();
        // 리소스 갯수가 2개 이상인 경우만 포함.
        builder.and(games.gameResources.size().goe(2));

        if (title != null) {
            builder.and(games.title.containsIgnoreCase(title)
                    .or(resources.title.containsIgnoreCase(title))
                    .or(users.nickname.containsIgnoreCase(title).and(games.isNamePrivate.eq(false))));
        }

        // DB에 등록되지 않은 카테고리가 있으면 0 으로 표시하기 위해 전체 초기화
        for (Category cat : Category.values()) {
            counts.put(cat, 0L);
        }

        List<Tuple> result = jpaQueryFactory
                .select(category.category, games.id.countDistinct())
                .from(category)
                .join(category.games, games)
                .join(games.users, users)
                .join(games.gameResources, resources)
                .where(builder)
                .groupBy(category.category)
                .fetch();

        // DB 에 존재하는 데이터는 덮어쓰기
        for (Tuple tuple : result) {
            Category cat = tuple.get(category.category);
            Long count = Optional.ofNullable(tuple.get(games.id.countDistinct())).orElse(0L);
            counts.put(cat, count);
        }

        int total = counts.values().stream()
                .mapToInt(Long::intValue)
                .sum();

        return GameCategoryNumsResponse.builder()
                .totalNums(total)
                .categoryNums(counts)
                .build();
    }

    @Override
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest) {
        QGamesEntity games = QGamesEntity.gamesEntity;
        QUsersEntity users = QUsersEntity.usersEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGameCategoryEntity gameCategory = QGameCategoryEntity.gameCategoryEntity;
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        BooleanBuilder builder = new BooleanBuilder();
        BooleanBuilder totalBuilder = new BooleanBuilder();

        this.setOptions(builder, totalBuilder, cursorId, searchRequest, games, users, resources, results, gameCategory);
        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<Tuple> resultTuples = jpaQueryFactory.selectDistinct(
                        games.id,
                        games.title,
                        games.description,
                        games.users.nickname,
                        images.fileUrl,
                        games.isNamePrivate,
                        games.createdDate,
                        games.isBlind
                ).from(games)
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .leftJoin(games.gameResources, resources)
                .leftJoin(games.categories, gameCategory)
                .leftJoin(games.users, users)
                .leftJoin(images).on(images.users.uid.eq(games.users.uid))
                .where(builder)
                .groupBy(games.id)
                .having(games.gameResources.size().goe(2))
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<GameListResponse> resultList = resultTuples.stream().map(tuple -> {
            Long roomId = tuple.get(games.id);
            String title = tuple.get(games.title);
            String description = tuple.get(games.description);
            String nickname = tuple.get(games.users.nickname);
            String profileImageUrl = tuple.get(images.fileUrl);
            boolean isPrivate = Boolean.TRUE.equals(tuple.get(games.isNamePrivate));
            OffsetDateTime createdAt = tuple.get(games.createdDate);
            Boolean isBlind = tuple.get(games.isBlind);

            if (isPrivate) {
                nickname = "익명";
            }

            List<Tuple> tuples = jpaQueryFactory.select(
                            resources.id,
                            resources.images.fileUrl.coalesce(resources.links.urls),
                            resources.images.mediaType.coalesce(resources.links.mediaType),
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
                            .type(tuples.getFirst().get(resources.images.mediaType.coalesce(resources.links.mediaType)))
                            .content(tuples.getFirst().get(resources.images.fileUrl.coalesce(resources.links.urls)))
                            .build()
                    : null;

            GameListSelectionResponse rightSelection = (!tuples.isEmpty()) ?
                    GameListSelectionResponse.builder()
                            .id(tuples.getLast().get(resources.id))
                            .title(tuples.getLast().get(resources.title))
                            .type(tuples.getLast().get(resources.images.mediaType.coalesce(resources.links.mediaType)))
                            .content(tuples.getLast().get(resources.images.fileUrl.coalesce(resources.links.urls)))
                            .build()
                    : null;

            return GameListResponse.builder()
                    .roomId(roomId)
                    .title(title)
                    .description(description)
                    .categories(category)
                    .existsBlind(isBlind)
                    .totalPlayNums(totalPlayNums != null ? totalPlayNums.intValue() : 0)
                    .weekPlayNums(weekPlayNums != null ? weekPlayNums.intValue() : 0)
                    .createdAt(createdAt)
                    .userResponse(UserMainResponse.builder()
                            .nickname(nickname)
                            .profileImageUrl(profileImageUrl)
                            .build())
                    .leftSelection(leftSelection)
                    .rightSelection(rightSelection)
                    .build();
        }).collect(Collectors.toList());    // toList() 는 불변 리스트로 반환되기 Collectors 로 한번 감싸줘야 함.

        boolean hasNext = PaginationUtils.hasNextPage(resultList, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(resultList, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(games.id.countDistinct())
                .from(games)
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .leftJoin(games.gameResources, resources)
                .leftJoin(games.categories, gameCategory)
                .leftJoin(games.users, users)
                .where(totalBuilder)
                .groupBy(games.id)
                .having(games.gameResources.size().goe(2))
                .fetchOne();

        return new CustomPageImpl<>(resultList, pageable, totalElements != null ? totalElements : 0L, cursorId, hasNext);
    }

    private void setOptions(BooleanBuilder builder, BooleanBuilder totalBuilder, Long cursorId,
                            GameSearchRequest request, QGamesEntity games, QUsersEntity users,
                            QGameResourcesEntity resources, QGameResultsEntity results,
                            QGameCategoryEntity gameCategory) {
        if (cursorId != null && request.getSortType().equals(GameSortType.OLD)) {
            builder.and(games.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameSortType.RECENT)) {
            builder.and(games.id.lt(cursorId));
        }

        if (request.getSortType().equals(GameSortType.WEEK)) {
            builder.and(results.createdDate.isNull().or(results.createdDate.after(OffsetDateTime.now().minusWeeks(1))));
            totalBuilder.and(results.createdDate.isNull().or(results.createdDate.after(OffsetDateTime.now().minusWeeks(1))));
        }

        if (request.getSortType().equals(GameSortType.MONTH)) {
            builder.and(results.createdDate.isNull().or(results.createdDate.after(OffsetDateTime.now().minusMonths(1))));
            totalBuilder.and(results.createdDate.isNull().or(results.createdDate.after(OffsetDateTime.now().minusMonths(1))));
        }

        if (request.getCategory() != null) {
            builder.and(gameCategory.category.in(request.getCategory()));
            totalBuilder.and(gameCategory.category.in(request.getCategory()));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(games.title.containsIgnoreCase(request.getTitle())
                    .or(resources.title.containsIgnoreCase(request.getTitle()))
                    .or(users.nickname.containsIgnoreCase(request.getTitle()).and(games.isNamePrivate.eq(false))));

            totalBuilder.and(games.title.containsIgnoreCase(request.getTitle())
                    .or(resources.title.containsIgnoreCase(request.getTitle()))
                    .or(users.nickname.containsIgnoreCase(request.getTitle()).and(games.isNamePrivate.eq(false))));
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(GameSortType sortType) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        return switch (sortType) {
            case OLD -> games.id.asc();
            case WEEK, MONTH, PLAY_DESC -> games.gamePlayList.size().desc();
            default -> games.id.desc();
        };
    }
}
