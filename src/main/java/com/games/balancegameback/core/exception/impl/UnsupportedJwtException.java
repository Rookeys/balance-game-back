package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class UnsupportedJwtException extends BusinessException {

    public UnsupportedJwtException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
