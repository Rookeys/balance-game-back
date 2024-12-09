package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class SignatureException extends BusinessException {

    public SignatureException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
