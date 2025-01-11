package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Links extends Media {

    private String urls;
    private int startSec;
    private int endSec;

    @Builder
    public Links(Long id, Games games, Users users, String urls, int startSec, int endSec) {
        super(id, games, users);
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
    }

    public void update(String urls, int startSec, int endSec) {
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
    }
}
