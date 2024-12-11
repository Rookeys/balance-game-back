package com.games.balancegameback.domain.media;

import lombok.Builder;

public record Links(Long id, String urls, int startSec, int endSec, Media media) {

    @Builder
    public Links {

    }
}
