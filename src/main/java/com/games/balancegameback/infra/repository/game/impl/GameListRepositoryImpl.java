package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GameListRepositoryImpl implements GameListRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest) {
        QGamesEntity games = QGamesEntity.gamesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        BooleanBuilder builder = new BooleanBuilder();
        BooleanBuilder totalBuilder = new BooleanBuilder();

        this.setOptions(builder, totalBuilder, cursorId, searchRequest, games, results);
        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<Tuple> resultTuples = jpaQueryFactory.selectDistinct(
                        games.id,
                        games.title,
                        games.description,
                        games.users.nickname,
                        games.isNamePublic,
                        results.id.count()
                ).from(games)
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .where(builder)
                .groupBy(games.id, games.title, games.description)
                .having(games.gameResources.size().goe(2))
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<GameListResponse> resultList = resultTuples.stream().map(tuple -> {
            Long roomId = tuple.get(games.id);
            String title = tuple.get(games.title);
            String description = tuple.get(games.description);
            String nickname = tuple.get(games.users.nickname);
            boolean isPublic = Boolean.TRUE.equals(tuple.get(games.isNamePublic));
            Long totalPlayNums = tuple.get(results.id.count());

            if (isPublic) {
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
                    .nickname(nickname)
                    .title(title)
                    .description(description)
                    .totalPlayNums(totalPlayNums != null ? totalPlayNums.intValue() : 0)
                    .leftSelection(leftSelection)
                    .rightSelection(rightSelection)
                    .build();
        }).collect(Collectors.toList());    // toList() 는 불변 리스트로 반환되기 Collectors 로 한번 감싸줘야 함.

        boolean hasNext = PaginationUtils.hasNextPage(resultList, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(resultList, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(games.count())
                .from(games)
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .where(totalBuilder)
                .fetchOne();

        return new CustomPageImpl<>(resultList, pageable, totalElements, cursorId, hasNext);
    }

    private void setOptions(BooleanBuilder builder, BooleanBuilder totalBuilder, Long cursorId,
                            GameSearchRequest request, QGamesEntity games, QGameResultsEntity results) {
        if (cursorId != null && request.getSortType().equals(GameSortType.idAsc)) {
            builder.and(games.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameSortType.idDesc)) {
            builder.and(games.id.lt(cursorId));
        }

        if (request.getSortType().equals(GameSortType.week)) {
            builder.and(results.createdDate.after(LocalDateTime.now().minusWeeks(1)));
            totalBuilder.and(results.createdDate.after(LocalDateTime.now().minusWeeks(1)));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(games.title.containsIgnoreCase(request.getTitle()));
            totalBuilder.and(games.title.containsIgnoreCase(request.getTitle()));
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(GameSortType sortType) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        return switch (sortType) {
            case idAsc -> games.id.asc();
            case week, playDesc -> games.gamePlayList.size().desc();
            default -> games.id.desc();
        };
    }
}
