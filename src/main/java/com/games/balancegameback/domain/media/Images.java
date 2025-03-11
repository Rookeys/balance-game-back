package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Images extends Media {

    private String fileUrl;
    private final MediaType mediaType;

    @Builder
    public Images(Long id, Games games, Users users, MediaType mediaType, String fileUrl) {
        super(id, games, users, mediaType);
        this.fileUrl = fileUrl;
        this.mediaType = mediaType;
    }

    public void update(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
