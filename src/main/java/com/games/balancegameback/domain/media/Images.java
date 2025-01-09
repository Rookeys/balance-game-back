package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Images extends Media {

    private String fileUrl;

    @Builder
    public Images(Long id, Games games, Users users, String fileUrl) {
        super(id, games, users);
        this.fileUrl = fileUrl;
    }

    public void update(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
