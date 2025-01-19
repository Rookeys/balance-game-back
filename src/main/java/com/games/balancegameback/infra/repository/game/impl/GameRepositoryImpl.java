package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameResponse;
import com.games.balancegameback.dto.game.GameStatusResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameJpaRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    public Page<GameListResponse> findGamesWithResources(Long cursorId, Users users, Pageable pageable) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        List<GameStatusResponse> results = jpaQueryFactory
                .select(Projections.constructor(GameStatusResponse.class,
                        games.id.as("roomId"),
                        games.title,
                        games.description
                ))
                .from(games)
                .join(games.users).on(games.users.email.eq(users.getEmail()))
                .where(cursorId == null ? games.id.loe(this.findMaxId(users)) : games.id.lt(cursorId))
                .orderBy(games.id.desc())
                .limit(pageable.getPageSize() + 1) // 다음 페이지 확인을 위해 +1
                .fetch();

        boolean hasNext = false;    // 요청보다 한 개 더 가져와서 hasNext 여부 판단.
        if (results.size() > pageable.getPageSize()) {
            results.removeLast();   // 마지막 요소를 제거해 페이징 크기를 유지.
            hasNext = true;
        }

        return new PageImpl<>(this.addThumbnailResources(results), pageable,
                hasNext ? pageable.getPageSize() + 1 : results.size());
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

    private Long findMaxId(Users users) {
        QGamesEntity games = QGamesEntity.gamesEntity;

        return jpaQueryFactory
                .select(games.id.max())
                .from(games)
                .where(games.users.email.eq(users.getEmail()))
                .fetchOne();
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
