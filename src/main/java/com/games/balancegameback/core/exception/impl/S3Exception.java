package com.games.balancegameback.core.exception.impl;

import com.games.balancegameback.core.exception.ErrorCode;

public class S3Exception extends BusinessException {
    public S3Exception(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
