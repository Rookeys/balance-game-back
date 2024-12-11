package com.games.balancegameback.domain.media;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.media.enums.UsingType;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;

public record Media(Long id, MediaType mediaType, UsingType usingType, Users user, Games game) {

    @Builder
    public Media {

    }
}
