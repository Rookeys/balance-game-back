package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceChildrenCommentResponse;
import com.games.balancegameback.dto.game.comment.GameResourceParentCommentResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceCommentJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
    public CustomPageImpl<GameResourceParentCommentResponse> findByGameResourceComments(Long resourceId, Long cursorId,
                                                                                        Users users, Pageable pageable,
                                                                                        GameCommentSearchRequest request) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;
        QUsersEntity user = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.gameResources.id.eq(resourceId));
        builder.and(comments.parent.isNull());  // 부모 댓글만 찾아옴.

        this.setOptions(builder, cursorId, request, comments);
        // 비로그인 회원은 좋아요를 표시했는지 안했는지 모르기 때문에 조건 추가.
        BooleanExpression leftJoinCondition = users != null ? comments.users.email.eq(users.getEmail()) : Expressions.FALSE;

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceParentCommentResponse> list = jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        GameResourceParentCommentResponse.class,
                        comments.id.as("commentId"),
                        comments.comment.as("comment"),
                        user.nickname.as("nickname"),
                        comments.children.size().as("children"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.isDeleted.as("isDeleted"),
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
    public CustomPageImpl<GameResourceChildrenCommentResponse> findByGameResourceChildrenComments(Long parentId, Long cursorId,
                                                                                                  Users users, Pageable pageable,
                                                                                                  GameCommentSearchRequest request) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;
        QUsersEntity user = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.parent.id.eq(parentId));   // 대댓글만 찾아옴.

        this.setOptions(builder, cursorId, request, comments);
        // 비로그인 회원은 좋아요를 표시했는지 안했는지 모르기 때문에 조건 추가.
        BooleanExpression leftJoinCondition = users != null ? comments.users.email.eq(users.getEmail()) : Expressions.FALSE;

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceChildrenCommentResponse> list = jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        GameResourceChildrenCommentResponse.class,
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
    public boolean existsByResourceIdAndParentId(Long resourceId, Long parentId) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        if (parentId == null) {
            return false;
        }

        BooleanExpression condition = comments.gameResources.id.eq(resourceId)
                .and(comments.parent.id.eq(parentId));

        Integer count = jpaQueryFactory
                .selectOne()
                .from(comments)
                .where(condition)
                .fetchFirst();

        return count != null;
    }


    private void setOptions(BooleanBuilder builder, Long cursorId, GameCommentSearchRequest request,
                            QGameResourceCommentsEntity comments) {
        if (cursorId != null && request.getSortType().equals(CommentSortType.idAsc)) {
            builder.and(comments.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(CommentSortType.idDesc)) {
            builder.and(comments.id.lt(cursorId));
        }

        if (request.getContent() != null && !request.getContent().isEmpty()) {
            builder.and(comments.comment.containsIgnoreCase(request.getContent()));
        }
    }

    // 정렬 방식 결정 쿼리
    private OrderSpecifier<?> getOrderSpecifier(CommentSortType sortType) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        return switch (sortType) {
            case likeAsc -> comments.likes.size().asc();
            case likeDesc -> comments.likes.size().desc();
            case idDesc -> comments.id.desc();
            default -> comments.id.asc();
        };
    }

    // 미사용 -> 추후 메인 페이지 작업 때 사용 예정
//    private BooleanExpression getRecentFilter(RecentSearchType recentSearchType) {
//        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
//        LocalDateTime now = LocalDateTime.now();
//
//        return switch (recentSearchType) {
//            case DAY -> comments.createdDate.after(now.minusDays(1));   // 최근 1일
//            case WEEK -> comments.createdDate.after(now.minusWeeks(1)); // 최근 1주
//            case MONTH -> comments.createdDate.after(now.minusMonths(1)); // 최근 1달
//        };
//    }

    // 좋아요를 눌렀는지 안 눌렀는지 확인하는 서브 쿼리
    private BooleanExpression isLikedExpression(Users users) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QGameCommentLikesEntity commentLikes = QGameCommentLikesEntity.gameCommentLikesEntity;

        return users != null ? JPAExpressions.selectOne()
                .from(commentLikes)
                .where(commentLikes.resourceComments.id.eq(comments.id)
                        .and(commentLikes.users.email.eq(users.getEmail())))
                .exists()
                : Expressions.asBoolean(false);
    }
}
