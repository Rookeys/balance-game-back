package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
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

    public CustomPageImpl<GameResourceParentCommentResponse> getParentCommentsByGameResource(Long resourceId, Long cursorId,
                                                                                             Pageable pageable,
                                                                                             GameCommentSearchRequest searchRequest,
                                                                                             HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return commentsRepository.findByGameResourceComments(resourceId, cursorId, users, pageable, searchRequest);
    }

    public CustomPageImpl<GameResourceChildrenCommentResponse> getChildrenCommentsByGameResource(Long parentId, Long cursorId,
                                                                                                 Pageable pageable,
                                                                                                 GameCommentSearchRequest searchRequest,
                                                                                                 HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return commentsRepository.findByGameResourceChildrenComments(parentId, cursorId, users, pageable, searchRequest);
    }

    @Transactional
    public void addComment(Long resourceId, GameResourceCommentRequest commentRequest,
                           HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResources gameResources = gameResourceRepository.findById(resourceId);

        if (commentsRepository.existsByParentId(commentRequest.getParentId())) {
            throw new BadRequestException("대댓글에 답글을 달 수 없습니다.", ErrorCode.RUNTIME_EXCEPTION);
        }

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
    public void updateComment(Long commentId, GameResourceCommentUpdateRequest commentRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResourceComments comments = commentsRepository.findById(commentId);

        if (!comments.getUsers().getEmail().equals(users.getEmail())) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        comments.update(commentRequest.getComment());
        commentsRepository.update(comments);
    }

    @Transactional
    public void deleteComment(Long commentId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResourceComments comments = commentsRepository.findById(commentId);

        if (!comments.getUsers().getEmail().equals(users.getEmail())) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (comments.getParentId() == null && comments.getChildren() != null) {
            comments.setDeleted(true);
            commentsRepository.update(comments);
        } else {
            commentsRepository.delete(comments);
        }
    }
}
