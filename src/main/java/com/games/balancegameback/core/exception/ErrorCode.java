package com.games.balancegameback.core.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ErrorCode {

    RUNTIME_EXCEPTION(400, "400", "400 Bad Request"),
    INVITE_CODE_NULL_EXCEPTION(400, "400_1", "Invite Code Null"),
    CLOSED_PLAYROOM_EXCEPTION(400, "400_2", "Already Closed PlayRoom!!"),
    ACCESS_DENIED_EXCEPTION(401, "401", "401 UnAuthorized"),
    NOT_ALLOW_WRITE_EXCEPTION(401, "401_1", "Not Allow"),
    NOT_ALLOW_RESIGN_EXCEPTION(401, "401_2", "Already resigned user."),
    NOT_ALLOW_OTHER_FORMATS(401, "401_3", "Not Allow Format"),
    DUPLICATED_EXCEPTION(401, "401_4", "Duplicated data"),
    NOT_ALLOW_NO_ACCESS(401, "401_5", "Don't have access"),
    FORBIDDEN_EXCEPTION(403, "403", "403 Forbidden"),
    NOT_FOUND_EXCEPTION(404, "404", "404 Not Found"),
    CONFLICT_EXCEPTION(409, "409", "409 Conflict"),
    KAKAO_ACCESS_TOKEN_FAILED(4001, "K4001", "Failed to get access token!"),
    KAKAO_USER_INFO_FAILED(4001, "K4002", "Failed to get user info!"),
    INVALID_TOKEN_EXCEPTION(4001, "4001", "Invalid JWT token"),
    JWT_TOKEN_EXPIRED(4002, "4002", "JWT token has expired"),
    UNSUPPORTED_JWT_TOKEN(4003, "4003", "JWT token is unsupported"),
    EMPTY_JWT_CLAIMS( 4004, "4004", "JWT claims string is empty"),
    JWT_SIGNATURE_MISMATCH(4005, "4005", "JWT signature does not match"),
    JWT_COMPLEX_ERROR(4006, "4006", "JWT Complex error"),
    JWT_NOT_ALLOW_REQUEST(4007, "4007", "JWT not allow request"),
    JWT_BLACKLIST(4008, "4008", "This token is already blacklisted or is not present.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
