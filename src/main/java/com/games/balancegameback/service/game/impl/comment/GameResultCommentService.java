package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.*;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.game.repository.GameResultCommentRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameResultCommentService {

    private final GameResultCommentRepository commentsRepository;
    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    public CustomPageImpl<GameResultCommentResponse> getCommentsByGameResult(Long gameId, Long cursorId, Pageable pageable,
                                                                             GameCommentSearchRequest searchRequest,
                                                                             HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return commentsRepository.findByGameResultComments(gameId, cursorId, users, pageable, searchRequest);
    }

    @Transactional
    public void addComment(Long gameId, GameResultCommentRequest commentRequest,
                           HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        Games games = gameRepository.findByRoomId(gameId);

        GameResultComments comments = GameResultComments.builder()
                .comment(commentRequest.getComment())
                .users(users)
                .games(games)
                .likes(null)
                .build();

        commentsRepository.save(comments);
    }

    @Transactional
    public void updateComment(Long commentId, GameResultCommentRequest commentRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResultComments comments = commentsRepository.findById(commentId);

        if (!comments.getUsers().getEmail().equals(users.getEmail())) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        comments.update(commentRequest.getComment());
        commentsRepository.update(comments);
    }

    @Transactional
    public void deleteComment(Long commentId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        GameResultComments comments = commentsRepository.findById(commentId);

        if (!comments.getUsers().getEmail().equals(users.getEmail())) {
            throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        commentsRepository.delete(comments);
    }
}
