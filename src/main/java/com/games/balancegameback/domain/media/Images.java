package com.games.balancegameback.domain.media;

import lombok.Builder;

public record Images(Long id, String fileName, String fileUrl) implements Media {

    @Builder
    public Images {

    }
}
