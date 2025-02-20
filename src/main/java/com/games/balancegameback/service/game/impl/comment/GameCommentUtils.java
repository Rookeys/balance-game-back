package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.domain.game.GameCommentLikes;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import com.games.balancegameback.service.game.repository.GameResultCommentRepository;
import com.games.balancegameback.service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GameCommentUtils {

    private final GameCommentLikesService likesService;
    private final GameCommentLikesRepository likesRepository;
    private final GameResourceCommentRepository resourceCommentsRepository;
    private final GameResultCommentRepository resultCommentsRepository;
    private final UserRepository usersRepository;

    @Transactional
    public void processLikes() {
        Set<String> keys = likesService.getAllLikeKeys();
        for (String key : keys) {
            String[] parts = key.split(":");
            boolean isResourceComment = parts[2].equals("resource");
            String email = parts[3];
            Long commentId = Long.valueOf(parts[4]);

            boolean finalState = likesService.getFinalLikeState(email, commentId, isResourceComment);
            processFinalLikeState(email, commentId, finalState, isResourceComment);
        }
    }

    private void processFinalLikeState(String email, Long commentId, boolean isLiked, boolean isResourceComment) {
        if (isLiked) {
            this.save(email, commentId, isResourceComment);
        } else {
            this.delete(email, commentId, isResourceComment);
        }
    }

    private void save(String email, Long commentId, boolean isResourceComment) {
        Users users = usersRepository.findByEmail(email);

        if (isResourceComment) {
            GameResourceComments comment = resourceCommentsRepository.findById(commentId);
            GameCommentLikes likes = GameCommentLikes.builder()
                    .users(users)
                    .resourceComments(comment)
                    .build();

            likesRepository.save(likes);
        } else {
            GameResultComments comment = resultCommentsRepository.findById(commentId);
            GameCommentLikes likes = GameCommentLikes.builder()
                    .users(users)
                    .resultComments(comment)
                    .build();

            likesRepository.save(likes);
        }
    }

    private void delete(String email, Long commentId, boolean isResourceComment) {
        if (isResourceComment) {
            likesRepository.deleteByUsersEmailAndResourceCommentsId(email, commentId);
        } else {
            likesRepository.deleteByUsersEmailAndResultCommentsId(email, commentId);
        }
    }
}
