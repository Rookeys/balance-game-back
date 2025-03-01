package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameJpaRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final GameJpaRepository gameRepository;
    private final JPAQueryFactory jpaQueryFactory;

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
        QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;

        BooleanBuilder builder = new BooleanBuilder();
        this.setOptions(builder, cursorId, searchRequest, games);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<GameStatusResponse> resultList = jpaQueryFactory
                .select(Projections.constructor(GameStatusResponse.class,
                        games.id.as("roomId"),
                        games.title,
                        games.description
                ))
                .from(games)
                .join(games.users).on(games.users.email.eq(users.getEmail()))
                .leftJoin(results).on(results.gameResources.games.eq(games))
                .where(builder)
                .orderBy(orderSpecifier)
                .groupBy(games.id)
                .limit(pageable.getPageSize() + 1) // 다음 페이지 확인을 위해 +1
                .fetch();

        boolean hasNext = PaginationUtils.hasNextPage(resultList, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(resultList, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(games.count())
                .from(games)
                .where(games.users.email.eq(users.getEmail()))
                .fetchOne();

        return new CustomPageImpl<>(this.addThumbnailResources(resultList), pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public boolean existsByIdAndUsers(Long gameId, Users users) {
        return gameRepository.existsByIdAndUsers(gameId, UsersEntity.from(users));
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

    private List<GameListResponse> addThumbnailResources(List<GameStatusResponse> results) {
        List<GameListResponse> list = new ArrayList<>();

        // 추후 게임 통계 기능이 개발되면 수정 예정.
        for (GameStatusResponse gameStatusResponse : results) {
            GameListResponse response = GameListResponse.builder()
                    .roomId(gameStatusResponse.getRoomId())
                    .description(gameStatusResponse.getDescription())
                    .title(gameStatusResponse.getTitle())
                    .leftContent(null)
                    .rightContent(null)
                    .leftTitle(null)
                    .rightTitle(null)
                    .build();

            list.add(response);
        }

        return list;
    }
}
