package com.games.balancegameback.domain.media;

import lombok.Builder;

public record Images(Long id, String fileName, String fileUrl, Media media) {

    @Builder
    public Images {

    }
}
