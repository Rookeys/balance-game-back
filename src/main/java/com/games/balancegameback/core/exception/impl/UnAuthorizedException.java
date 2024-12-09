package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class UnAuthorizedException extends BusinessException {

    public UnAuthorizedException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
