package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import lombok.Getter;

@Getter
public class Media {

    private final Long id;
    private final Games games;
    private final Users users;

    public Media(Long id, Games games, Users users) {
        this.id = id;
        this.games = games;
        this.users = users;
    }
}
