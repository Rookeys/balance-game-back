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
import com.games.balancegameback.infra.repository.game.common.AbstractGameRepository;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

@Repository
public class GameRepositoryImpl extends AbstractGameRepository implements GameRepository {

    private final GameJpaRepository gameRepository;

    public GameRepositoryImpl(JPAQueryFactory jpaQueryFactory,
                             CommonGameRepository commonGameRepository,
                             GameJpaRepository gameRepository) {
        super(jpaQueryFactory, commonGameRepository);
        this.gameRepository = gameRepository;
    }

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
        return findGameListAutomated(cursorId, pageable, searchRequest, users, GameListType.MY_GAMES);
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
