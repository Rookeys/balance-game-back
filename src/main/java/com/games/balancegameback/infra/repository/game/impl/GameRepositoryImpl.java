package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameResponse;
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

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameRepositoryImpl implements GameRepository {

    private final GameJpaRepository gameRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(Games games) {
        gameRepository.save(GamesEntity.from(games));
    }

    @Override
    public GameResponse findById(Long roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        Games games = gamesEntity.toModel();
        String inviteCode = null;

        if (games.accessType().equals(AccessType.PROTECTED)) {
            inviteCode = "inviteCode 로직 완성 후 수정 예정.";
        }

        return GameResponse.builder()
                .roomId(roomId)
                .title(games.title())
                .description(games.description())
                .isNamePublic(games.isNamePublic())
                .accessType(games.accessType())
                .inviteCode(inviteCode)
                .category(games.category())
                .build();
    }

    @Override
    public Page<GameListResponse> findGamesWithResources(Long cursorId, Users users, Pageable pageable) {
        QGamesEntity games = QGamesEntity.gamesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        if (cursorId == null) {
            cursorId = jpaQueryFactory
                    .select(games.id.max())
                    .from(games)
                    .where(games.users.email.eq(users.getEmail()))
                    .fetchOne();
        }

        List<GameListResponse> results = jpaQueryFactory
                .select(Projections.constructor(GameListResponse.class,
                        games.id.as("roomId"),
                        games.title,
                        games.description,
                        // 이 부분은 Game Result 가 구현된 이후 랭킹 1, 2등 썸네일을 받아오는 것으로 교체 예정
                        resources.images.fileUrl.coalesce(resources.links.urls).as("leftContent"),
                        resources.links.urls.coalesce(resources.images.fileUrl).as("rightContent"),
                        resources.title.as("leftTitle"),
                        resources.title.as("rightTitle")
                ))
                .from(games)
                .leftJoin(resources).on(resources.games.id.eq(games.id))
                .leftJoin(resources.images, images)
                .leftJoin(resources.links, links)
                .join(games.users).on(games.users.email.eq(users.getEmail()))
                .where(cursorId != null ? games.id.lt(cursorId) : null)
                .orderBy(games.id.desc())
                .limit(pageable.getPageSize() + 1) // 다음 페이지 확인을 위해 +1
                .fetch();

        boolean hasNext = false;    // 요청보다 한 개 더 가져와서 hasNext 여부 판단.
        if (results.size() > pageable.getPageSize()) {
            results.removeLast();   // 마지막 요소를 제거해 페이징 크기를 유지.
            hasNext = true;
        }

        return new PageImpl<>(results, pageable, hasNext ? pageable.getPageSize() + 1 : results.size());
    }

    @Override
    public boolean existsByIdAndUsers(Long roomId, Users users) {
        return gameRepository.existsByIdAndUsers(roomId, UsersEntity.from(users));
    }

    @Override
    public void update(Games games) {
        GamesEntity gamesEntity = GamesEntity.from(games);
        gameRepository.save(gamesEntity);
    }

    @Override
    public void deleteById(Long roomId) {
        gameRepository.deleteById(roomId);
    }
}
