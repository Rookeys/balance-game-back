package com.games.balancegameback.core.exception;

import lombok.Builder;

public record ErrorEntity(int status, String errorCode, String errorMessage) {

    @Builder
    public ErrorEntity {

    }
}
