package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class IllegalArgumentException extends BusinessException {

    public IllegalArgumentException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
