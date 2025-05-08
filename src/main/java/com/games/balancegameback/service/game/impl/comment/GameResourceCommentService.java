package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.*;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameResourceCommentService {

    private final GameResourceCommentRepository commentsRepository;
    private final GameResourceRepository gameResourceRepository;
    private final UserUtils userUtils;

    public CustomPageImpl<GameResourceParentCommentResponse> getParentCommentsByGameResource(Long gameId, Long resourceId,
                                                                                             Long cursorId, Pageable pageable,
                                                                                             GameCommentSearchRequest searchRequest,
                                                                                             HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return commentsRepository.findByGameResourceComments(gameId, resourceId, cursorId, users, pageable, searchRequest);
    }

    public CustomPageImpl<GameResourceChildrenCommentResponse> getChildrenCommentsByGameResource(Long gameId, Long resourceId,
                                                                                                 Long parentId, Long cursorId,
                                                                                                 Pageable pageable,
                                                                                                 GameCommentSearchRequest searchRequest,
                                                                                                 HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return commentsRepository.findByGameResourceChildrenComments(gameId, resourceId, parentId, cursorId,
                users, pageable, searchRequest);
    }

    @Transactional
    public void addComment(Long gameId, Long resourceId, GameResourceCommentRequest commentRequest,
                           HttpServletRequest request) {

        if (!commentsRepository.existsByGameIdAndResourceId(gameId, resourceId)) {
            throw new NotFoundException("해당 리소스는 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        if (commentRequest.getParentId() != null && commentsRepository.isChildComment(commentRequest.getParentId())) {
            throw new BadRequestException("대댓글에 답글을 달 수 없습니다.", ErrorCode.RUNTIME_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);
        GameResources gameResources = gameResourceRepository.findById(resourceId);

        GameResourceComments comments = GameResourceComments.builder()
                .comment(commentRequest.getComment())
                .isDeleted(false)
                .users(users)
                .gameResources(gameResources)
                .parentId(commentRequest.getParentId())
                .children(null)
                .likes(null)
                .build();

        commentsRepository.save(comments);
    }

    @Transactional
    public void updateComment(Long gameId, Long resourceId, Long commentId, GameResourceCommentUpdateRequest commentRequest,
                              HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResourceComments comments = commentsRepository.findById(commentId);

        boolean matchesGame = comments.getGameResources().getGames().getId().equals(gameId);
        boolean matchesResource = comments.getGameResources().getId().equals(resourceId);
        boolean isAuthor = comments.getUsers().getEmail().equals(users.getEmail());

        if (!matchesGame || !matchesResource || !isAuthor) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (comments.isDeleted()) {
            throw new BadRequestException("이미 삭제된 댓글입니다.", ErrorCode.ALREADY_DELETED_COMMENT);
        }

        comments.update(commentRequest.getComment());
        commentsRepository.update(comments);
    }

    @Transactional
    public void deleteComment(Long gameId, Long resourceId, Long commentId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResourceComments comments = commentsRepository.findById(commentId);

        boolean matchesGame = comments.getGameResources().getGames().getId().equals(gameId);
        boolean matchesResource = comments.getGameResources().getId().equals(resourceId);
        boolean isAuthor = comments.getUsers().getEmail().equals(users.getEmail());

        if (!matchesGame || !matchesResource || !isAuthor) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (comments.isDeleted()) {
            throw new BadRequestException("이미 삭제된 댓글입니다.", ErrorCode.ALREADY_DELETED_COMMENT);
        }

        if (comments.getParentId() == null && !comments.getChildren().isEmpty()) {
            comments.setDeleted(true);
            commentsRepository.update(comments);
        } else {
            commentsRepository.delete(comments);
        }
    }
}
