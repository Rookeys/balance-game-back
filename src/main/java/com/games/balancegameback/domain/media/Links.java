package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Links extends Media {

    private Long id;
    private String urls;
    private int startSec;
    private int endSec;
    private final MediaType mediaType;

    @Builder
    public Links(Long id, Games games, Users users, MediaType mediaType, String urls, int startSec, int endSec) {
        super(id, games, users, mediaType);
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
        this.mediaType = mediaType;
    }

    public void update(String urls, int startSec, int endSec) {
        this.urls = urls;
        this.startSec = startSec;
        this.endSec = endSec;
    }
}
