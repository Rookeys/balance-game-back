package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.game.enums.RecentSearchType;
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
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                                                                                        Pageable pageable,
                                                                                        GameCommentSearchRequest request) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QUsersEntity users = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(resources.id.eq(resourceId));
        builder.and(comments.parent.isNull());  // 부모 댓글만 찾아옴.

        this.setOptions(builder, cursorId, request, comments);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceParentCommentResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceParentCommentResponse.class,
                        comments.id.as("commentId"),
                        comments.comment.as("comment"),
                        users.nickname.as("nickname"),
                        comments.children.size().as("children"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.isDeleted.as("isDeleted"),
                        comments.likes.size().as("like")
                ))
                .from(comments)
                .where(builder)
                .join(comments.users, users)
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
                                                                                                  Pageable pageable,
                                                                                                  GameCommentSearchRequest request) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        QUsersEntity users = QUsersEntity.usersEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comments.parent.id.eq(parentId));   // 대댓글만 찾아옴.

        this.setOptions(builder, cursorId, request, comments);

        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceChildrenCommentResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceChildrenCommentResponse.class,
                        comments.id.as("commentId"),
                        comments.comment.as("comment"),
                        users.nickname.as("nickname"),
                        comments.createdDate.as("createdDateTime"),
                        comments.updatedDate.as("updatedDateTime"),
                        comments.likes.size().as("like")
                ))
                .from(comments)
                .where(builder)
                .join(comments.users, users)
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
    public boolean existsByParentId(Long id) {
        return gameResourceCommentRepository.existsByParentId(id);
    }

    private void setOptions(BooleanBuilder builder, Long cursorId, GameCommentSearchRequest request,
                            QGameResourceCommentsEntity comments) {
        if (cursorId != null) {
            builder.and(comments.id.gt(cursorId));
        }

        if (request.getContent() != null && !request.getContent().isEmpty()) {
            builder.and(comments.comment.containsIgnoreCase(request.getContent()));
        }

        if (request.getRecentSearchType() != null) {
            builder.and(getRecentFilter(request.getRecentSearchType()));
        }
    }

    private OrderSpecifier<?> getOrderSpecifier(CommentSortType sortType) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;

        if (sortType == null) {
            return comments.id.asc();
        }

        return switch (sortType) {
            case likeAsc -> comments.likes.size().asc();
            case likeDesc -> comments.likes.size().desc();
            case idDesc -> comments.id.desc();
            default -> comments.id.asc();
        };
    }

    private BooleanExpression getRecentFilter(RecentSearchType recentSearchType) {
        QGameResourceCommentsEntity comments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
        LocalDateTime now = LocalDateTime.now();

        return switch (recentSearchType) {
            case DAY -> comments.createdDate.after(now.minusDays(1));   // 최근 1일
            case WEEK -> comments.createdDate.after(now.minusWeeks(1)); // 최근 1주
            case MONTH -> comments.createdDate.after(now.minusMonths(1)); // 최근 1달
        };
    }
}
