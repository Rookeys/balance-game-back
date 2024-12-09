package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
