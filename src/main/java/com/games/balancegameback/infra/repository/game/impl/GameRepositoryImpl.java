package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.Games;
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
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
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

        Games games = gamesEntity.toModel();

        return GameResponse.builder()
                .roomId(roomId)
                .title(games.getTitle())
                .description(games.getDescription())
                .isNamePublic(games.getIsNamePublic())
                .accessType(games.getAccessType())
                .inviteCode(games.getGameInviteCode().getInviteCode())
                .category(games.getCategory())
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
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        BooleanBuilder builder = new BooleanBuilder();
        this.setOptions(builder, cursorId, searchRequest, games);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<Tuple> resultTuples = jpaQueryFactory.select(
                        games.id,
                        games.title,
                        games.description,
                        games.users.nickname,
                        images.fileUrl,
                        games.createdDate
                ).from(games)
                .join(games.users).on(games.users.uid.eq(users.getUid()))
                .leftJoin(results).on(results.gameResources.games.eq(games))
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

        boolean hasNext = PaginationUtils.hasNextPage(resultList, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(resultList, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(games.count())
                .from(games)
                .where(games.users.email.eq(users.getEmail()))
                .fetchOne();

        return new CustomPageImpl<>(resultList, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public boolean existsByIdAndUsers(Long gameId, Users users) {
        return gameRepository.existsByIdAndUsers(gameId, UsersEntity.from(users));
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

    @Override
    public void deleteById(Long roomId) {
        gameRepository.deleteById(roomId);
    }

    @Override
    public void deleteImagesInS3(Long roomId) {
        GamesEntity entity = gameRepository.findById(roomId).orElseThrow(()
                -> new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        List<String> imageUrls = entity.getGameResources().stream()
                .filter(resources -> resources.getImages() != null)
                .map(resources -> resources.getImages().getFileUrl())
                .toList();

        s3Service.deleteImagesAsync(imageUrls);
    }

    private void setOptions(BooleanBuilder builder, Long cursorId,
                            GameSearchRequest request, QGamesEntity games) {
        if (cursorId != null && request.getSortType().equals(GameSortType.idAsc)) {
            builder.and(games.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameSortType.idDesc)) {
            builder.and(games.id.lt(cursorId));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(games.title.containsIgnoreCase(request.getTitle()));
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(GameSortType sortType) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        // 나중에 조건 추가를 고려해 switch 유지
        return switch (sortType) {
            case idAsc -> games.id.asc();
            default -> games.id.desc();
        };
    }
}
