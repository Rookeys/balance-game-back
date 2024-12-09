package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class DuplicateException extends BusinessException {

    public DuplicateException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
