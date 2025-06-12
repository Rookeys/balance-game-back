package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
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

    // 나머지 메서드들도 String 타입으로 수정
    @Override
    public Games findByRoomId(String roomId) {
        GamesEntity gamesEntity = gameRepository.findById(roomId).orElseThrow(() ->
                new NotFoundException("해당 게임방은 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        UsersEntity userEntity = userRepository.findById(gamesEntity.getUserId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        Games games = gamesEntity.toModel();
        games.setUsers(userEntity.toModel());
        return games;
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

    // 기타 메서드 구현 생략 (기존 로직 유지하되 타입만 변경)
}