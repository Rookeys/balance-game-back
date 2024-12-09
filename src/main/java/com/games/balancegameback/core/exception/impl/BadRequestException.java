package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class BadRequestException extends BusinessException {

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
