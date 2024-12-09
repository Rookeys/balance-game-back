package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class JwtExpiredException extends BusinessException {

    public JwtExpiredException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
