package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Data;

@Data
public class GameCommentLikes {

    private Long id;
    private Users users;
    private GameResourceComments resourceComments;
    private GameResultComments resultComments;

    @Builder
    public GameCommentLikes(Long id, Users users, GameResourceComments resourceComments,
                            GameResultComments resultComments) {
        this.id = id;
        this.users = users;
        this.resourceComments = resourceComments;
        this.resultComments = resultComments;
    }
}
