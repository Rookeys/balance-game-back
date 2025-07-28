package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceChildrenCommentResponse;
import com.games.balancegameback.dto.game.comment.GameResourceParentCommentResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceCommentJpaRepository;
import com.games.balancegameback.infra.repository.user.UserJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameResourceCommentRepositoryImpl implements GameResourceCommentRepository {

    private final GameResourceCommentJpaRepository gameResourceCommentRepository;
    private final UserJpaRepository userRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(GameResourceComments gameResourceComments) {
        gameResourceCommentRepository.save(GameResourceCommentsEntity.from(gameResourceComments));
    }

    @Override
    public GameResourceComments findById(Long id) {
        return gameResourceCommentRepository.findById(id).orElseThrow(() ->
                new NotFoundException("댓글이 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).toModel();
    }

    @Override
    public void update(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity entity = gameResourceCommentRepository.findById(gameResourceComments.getId())
                .orElseThrow(() -> new NotFoundException("해당하는 정보가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        entity.update(gameResourceComments);
    }

    @Override
    public void delete(GameResourceComments gameResourceComments) {
        gameResourceCommentRepository.delete(GameResourceCommentsEntity.from(gameResourceComments));
    }

    @Override
    public CustomPageImpl<GameResourceParentCommentResponse> findByGameResourceComments(Long gameId, Long resourceId, Long cursorId,
                                                                                        Users users, Pageable pageable,
                                                                                        GameCommentSearchRequest request) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGamesEntity games = QGamesEntity.gamesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QUsersEntity user = QUsersEntity.usersEntity; // 댓글 작성자
        QUsersEntity gameUser = new QUsersEntity("gameUser"); // 게임 제작자

        // gameId 와 resourceId 가 맞게 왔는지 확인.
        BooleanExpression condition = resources.id.eq(resourceId)
                .and(resources.games.id.eq(gameId));

        Integer result = jpaQueryFactory
                .selectOne()
                .from(resources)
                .where(condition)
                .fetchFirst();

        if (result == null) {
            throw new NotFoundException("존재하지 않는 경로입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        // gameId 와 resourceId 가 맞게 왔는지 확인.
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.gameResources.games.id.eq(gameId));
        builder.and(comments.gameResources.id.eq(resourceId));
        builder.and(comments.parent.isNull());

        this.setOptions(builder, cursorId, request, comments);

        BooleanExpression leftJoinCondition = users != null ? comments.users.uid.eq(users.getUid()) : Expressions.FALSE;
        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceParentCommentResponse> list = jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        GameResourceParentCommentResponse.class,
                        comments.id.as("commentId"),
                        new CaseBuilder()
                                .when(comments.isDeleted.isTrue())
                                .then("삭제된 댓글입니다.")
                                .otherwise(comments.comment)
                                .as("comment"),
                        new CaseBuilder()
                                .when(comments.users.uid.eq(gameUser.uid)
                                        .and(games.isNamePrivate.isTrue()))
                                .then("익명")
                                .when(user.nickname.startsWith("DELETED_USER_"))
                                .then("회원 탈퇴한 사용자")
                                .otherwise(user.nickname)
                                .as("nickname"),
                        new CaseBuilder()
                                .when(comments.users.uid.eq(gameUser.uid)
                                        .and(games.isNamePrivate.isTrue()))
                                .then("")
                                .otherwise(images.fileUrl)
                                .as("profileImageUrl"),
                        comments.children.size().as("children"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.isDeleted.as("isDeleted"),
                        comments.likes.size().as("like"),
                        this.isLikedExpression(users).as("existsLiked"),
                        comments.users.uid.eq(gameUser.uid).as("existsWriter"),
                        users != null ? comments.users.uid.eq(users.getUid()) : Expressions.asBoolean(false)
                ))
                .from(comments)
                .leftJoin(comments.users, user)
                .leftJoin(images).on(images.users.uid.eq(user.uid))
                .leftJoin(comments.gameResources, resources)
                .leftJoin(resources.games, games)
                .leftJoin(games.users, gameUser)
                .leftJoin(commentLikes).on(leftJoinCondition)
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = list.size() > pageable.getPageSize();

        if (hasNext) {
            list.removeLast(); // 안전한 마지막 요소 제거
        }

        Long totalElements = jpaQueryFactory
                .select(comments.count())
                .from(comments)
                .where(builder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public CustomPageImpl<GameResourceChildrenCommentResponse> findByGameResourceChildrenComments(Long gameId, Long resourceId,
                                                                                                  Long parentId, Long cursorId,
                                                                                                  Users users, Pageable pageable,
                                                                                                  GameCommentSearchRequest request) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGamesEntity games = QGamesEntity.gamesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QUsersEntity user = QUsersEntity.usersEntity; // 댓글 작성자
        QUsersEntity gameUser = new QUsersEntity("gameUser"); // 게임 제작자

        // gameId 와 resourceId, parentId 가 맞게 왔는지 확인.
        BooleanExpression condition = comments.id.eq(parentId)
                .and(comments.gameResources.id.eq(resourceId))
                .and(comments.gameResources.games.id.eq(gameId));

        Integer existCheck = jpaQueryFactory
                .selectOne()
                .from(comments)
                .where(condition)
                .fetchFirst();

        if (existCheck == null) {
            throw new NotFoundException("존재하지 않는 댓글 경로입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        // 데이터 실존 확인 이후 get 쿼리 준비.
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.gameResources.games.id.eq(gameId));
        builder.and(comments.gameResources.id.eq(resourceId));
        builder.and(comments.parent.id.eq(parentId));   // 대댓글만 찾아옴.

        this.setOptions(builder, cursorId, request, comments);
        // 비로그인 회원은 좋아요를 표시했는지 안했는지 모르기 때문에 조건 추가.
        BooleanExpression leftJoinCondition = users != null ? comments.users.uid.eq(users.getUid()) : Expressions.FALSE;
        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceChildrenCommentResponse> list = jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        GameResourceChildrenCommentResponse.class,
                        comments.id.as("commentId"),
                        comments.comment.as("comment"),
                        new CaseBuilder()
                                .when(comments.users.uid.eq(gameUser.uid)
                                        .and(games.isNamePrivate.isTrue()))
                                .then("익명")
                                .when(user.nickname.startsWith("DELETED_USER_"))
                                .then("회원 탈퇴한 사용자")
                                .otherwise(user.nickname)
                                .as("nickname"),
                        new CaseBuilder()
                                .when(comments.users.uid.eq(gameUser.uid)
                                        .and(games.isNamePrivate.isTrue()))
                                .then("")
                                .otherwise(images.fileUrl)
                                .as("profileImageUrl"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.likes.size().as("like"),
                        this.isLikedExpression(users).as("existsLiked"),
                        comments.users.uid.eq(gameUser.uid).as("existsWriter"),
                        users != null ? comments.users.uid.eq(users.getUid()) : Expressions.asBoolean(false)

                ))
                .from(comments)
                .leftJoin(comments.users, user)
                .leftJoin(images).on(images.users.uid.eq(user.uid))
                .leftJoin(comments.gameResources, resources)
                .leftJoin(resources.games, games)
                .leftJoin(games.users, gameUser)
                .leftJoin(commentLikes).on(leftJoinCondition)
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = list.size() > pageable.getPageSize();

        if (hasNext) {
            list.removeLast(); // 안전한 마지막 요소 제거
        }

        Long totalElements = jpaQueryFactory
                .select(comments.count())
                .from(comments)
                .where(builder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public boolean isChildComment(Long parentId) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        BooleanExpression condition = comments.id.eq(parentId)
                .and(comments.parent.isNotNull());

        Integer result = jpaQueryFactory
                .selectOne()
                .from(comments)
                .where(condition)
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsByGameIdAndResourceId(Long gameId, Long resourceId) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        BooleanExpression condition = resources.id.eq(resourceId)
                .and(resources.games.id.eq(gameId));

        Integer result = jpaQueryFactory
                .selectOne()
                .from(resources)
                .where(condition)
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsByGameIdAndCommentId(Long gameId, Long commentId) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        return jpaQueryFactory
                .selectOne()
                .from(comments)
                .where(comments.id.eq(commentId)
                        .and(comments.gameResources.games.id.eq(gameId)))
                .fetchFirst() != null;
    }

    private void setOptions(BooleanBuilder builder, Long cursorId, GameCommentSearchRequest request,
                            QGameResourceCommentsEntity comments) {
        if (cursorId != null && request.getSortType().equals(CommentSortType.OLD)) {
            builder.and(comments.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(CommentSortType.RECENT)) {
            builder.and(comments.id.lt(cursorId));
        }

        if (request.getSortType().equals(CommentSortType.LIKE_DESC) || request.getSortType().equals(CommentSortType.LIKE_ASC)) {
            this.applyOtherSortOptions(builder, cursorId, request, comments);
        }

        if (request.getContent() != null && !request.getContent().isEmpty()) {
            builder.and(comments.comment.containsIgnoreCase(request.getContent()));
        }
    }

    private void applyOtherSortOptions(BooleanBuilder builder, Long cursorId,
                                      GameCommentSearchRequest request, QGameResourceCommentsEntity comments) {
        NumberExpression<Integer> likeCount = comments.likes.size().coalesce(0);
        Integer cursorLikeCount = null;

        if (cursorId != null) {
            cursorLikeCount = jpaQueryFactory
                    .select(comments.likes.size().coalesce(0))
                    .from(comments)
                    .where(comments.id.eq(cursorId))
                    .fetchOne();
        }

        // cursorId 가 없다면 바로 탈출
        if (cursorId == null || cursorLikeCount == null) {
            return;
        }

        if (request.getSortType().equals(CommentSortType.LIKE_DESC)) {
            builder.and(
                    new BooleanBuilder()
                            .or(likeCount.lt(cursorLikeCount))
                            .or(likeCount.eq(cursorLikeCount).and(comments.id.gt(cursorId)))
            );
        }

        if (request.getSortType().equals(CommentSortType.LIKE_ASC)) {
            builder.and(
                    new BooleanBuilder()
                            .or(likeCount.gt(cursorLikeCount))
                            .or(likeCount.eq(cursorLikeCount).and(comments.id.gt(cursorId)))
            );
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(CommentSortType sortType) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        return switch (sortType) {
            case LIKE_ASC -> comments.likes.size().asc();
            case LIKE_DESC -> comments.likes.size().desc();
            case RECENT -> comments.id.desc();
            default -> comments.id.asc();
        };
    }

    // 좋아요를 눌렀는지 안 눌렀는지 확인하는 서브 쿼리
    private BooleanExpression isLikedExpression(Users users) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;

        return users != null ? JPAExpressions.selectOne()
                .from(commentLikes)
                .where(commentLikes.resourceComments.id.eq(comments.id)
                        .and(commentLikes.users.uid.eq(users.getUid())))
                .exists()
                : Expressions.asBoolean(false);
    }
}
