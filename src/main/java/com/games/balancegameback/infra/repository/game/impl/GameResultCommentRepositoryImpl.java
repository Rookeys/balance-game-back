package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResultCommentResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResultCommentJpaRepository;
import com.games.balancegameback.service.game.repository.GameResultCommentRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class GameResultCommentRepositoryImpl implements GameResultCommentRepository {

    private final GameResultCommentJpaRepository gameResultCommentJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(GameResultComments gameResultComments) {
        gameResultCommentJpaRepository.save(GameResultCommentsEntity.from(gameResultComments));
    }

    @Override
    public void update(GameResultComments gameResultComments) {
        GameResultCommentsEntity entity = gameResultCommentJpaRepository.findById(gameResultComments.getId())
                .orElseThrow(() -> new NotFoundException("해당하는 정보가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        entity.update(gameResultComments);
    }

    @Override
    public void delete(GameResultComments gameResultComments) {
        gameResultCommentJpaRepository.delete(GameResultCommentsEntity.from(gameResultComments));
    }

    @Override
    public CustomPageImpl<GameResultCommentResponse> findByGameResultComments(Long gameId, Long cursorId, Users users,
                                                                              Pageable pageable,
                                                                              GameCommentSearchRequest searchRequest) {
        QGameResultCommentsEntity comments = QGameResultCommentsEntity.gameResultCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;
        QUsersEntity user = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.games.id.eq(gameId));

        this.setOptions(builder, cursorId, searchRequest, comments);
        // 비로그인 회원은 좋아요를 표시했는지 안했는지 모르기 때문에 조건 추가.
        BooleanExpression leftJoinCondition = users != null ? comments.users.email.eq(users.getEmail()) : Expressions.FALSE;

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(searchRequest.getSortType());

        List<GameResultCommentResponse> list = jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        GameResultCommentResponse.class,
                        comments.id.as("commentId"),
                        comments.comment.as("comment"),
                        user.nickname.as("nickname"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.likes.size().as("like"),
                        this.isLikedExpression(users)
                ))
                .from(comments)
                .where(builder)
                .join(comments.users, user)
                .leftJoin(commentLikes).on(leftJoinCondition)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = PaginationUtils.hasNextPage(list, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(list, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(comments.count())
                .from(comments)
                .where(builder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public GameResultComments findById(Long id) {
        return gameResultCommentJpaRepository.findById(id).orElseThrow(() ->
                new NotFoundException("해당하는 데이터가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).toModel();
    }

    private void setOptions(BooleanBuilder builder, Long cursorId, GameCommentSearchRequest request,
                            QGameResultCommentsEntity comments) {
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
                                      GameCommentSearchRequest request, QGameResultCommentsEntity comments) {
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
        QGameResultCommentsEntity comments = QGameResultCommentsEntity.gameResultCommentsEntity;

        return switch (sortType) {
            case LIKE_ASC -> comments.likes.size().asc();
            case LIKE_DESC -> comments.likes.size().desc();
            case RECENT -> comments.id.desc();
            default -> comments.id.asc();
        };
    }

    // 좋아요를 눌렀는지 안 눌렀는지 확인하는 서브 쿼리
    private BooleanExpression isLikedExpression(Users users) {
        QGameResultCommentsEntity comments = QGameResultCommentsEntity.gameResultCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;

        return users != null ? JPAExpressions.selectOne()
                .from(commentLikes)
                .where(commentLikes.resultComments.id.eq(comments.id)
                        .and(commentLikes.users.email.eq(users.getEmail())))
                .exists()
                : Expressions.asBoolean(false);
    }
}
