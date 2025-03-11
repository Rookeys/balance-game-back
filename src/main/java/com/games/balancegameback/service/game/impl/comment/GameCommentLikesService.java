package com.games.balancegameback.service.game.impl.comment;

import com.games.balancegameback.domain.game.enums.CommentType;
import com.games.balancegameback.domain.game.GameCommentLikes;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import com.games.balancegameback.service.game.repository.GameResultCommentRepository;
import com.games.balancegameback.service.user.UserRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameCommentLikesService {

    private final UserUtils userUtils;
    private final GameCommentLikesRepository likesRepository;
    private final GameResourceCommentRepository resourceCommentsRepository;
    private final GameResultCommentRepository resultCommentsRepository;
    private final UserRepository usersRepository;

    /**
     * 클라이언트에서 2초 디바운싱을 걸었다는 전제 하에 설계 진행함.
     * 스케쥴러를 사용하여 자체적으로 3초 디바운싱을 걸어봤지만
     * 오버 엔지니어링이 되어 버려 설계를 변경함.
     */
    @Transactional
    public void toggleLike(Long commentId, boolean isLiked, CommentType commentType, HttpServletRequest request) {
        String email = userUtils.getEmail(request);
        boolean isResourceComment = commentType.equals(CommentType.RESOURCE);

        if (isLiked) {
            saveLike(email, commentId, isResourceComment);
        } else {
            deleteLike(email, commentId, isResourceComment);
        }
    }

    private void saveLike(String email, Long commentId, boolean isResourceComment) {
        Users users = usersRepository.findByEmail(email);
        if (isResourceComment) {
            GameResourceComments comment = resourceCommentsRepository.findById(commentId);
            if (!likesRepository.existsByUsersEmailAndResourceCommentsId(email, comment.getId())) {
                GameCommentLikes like = GameCommentLikes.builder()
                        .users(users)
                        .resourceComments(comment)
                        .build();
                likesRepository.save(like);
            }
        } else {
            GameResultComments comment = resultCommentsRepository.findById(commentId);
            if (!likesRepository.existsByUsersEmailAndResultCommentsId(email, comment.getId())) {
                GameCommentLikes like = GameCommentLikes.builder()
                        .users(users)
                        .resultComments(comment)
                        .build();
                likesRepository.save(like);
            }
        }
    }

    private void deleteLike(String email, Long commentId, boolean isResourceComment) {
        if (isResourceComment) {
            likesRepository.deleteByUsersEmailAndResourceCommentsId(email, commentId);
        } else {
            likesRepository.deleteByUsersEmailAndResultCommentsId(email, commentId);
        }
    }
}


