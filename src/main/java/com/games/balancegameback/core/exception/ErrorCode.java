package com.games.balancegameback.core.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ErrorCode {

    RUNTIME_EXCEPTION(400, "400", "400 Bad Request"),
    INVITE_CODE_NULL_EXCEPTION(400, "400_1", "Invite Code Null"),
    CLOSED_PLAYROOM_EXCEPTION(400, "400_2", "Already Closed PlayRoom!!"),
    INVALID_ROUND_EXCEPTION(400, "400_3", "Invalid Round"),
    FAILED_REVALIDATE_EXCEPTION(400, "400_4", "Failed Revalidate"),
    NOT_EXISTS_PARENTS(400, "400_5", "Not Exists ParentId"),
    NOT_WRITE_ETC(400, "400_6", "Not Write Etc"),
    INVALID_REPORT_TARGET_TYPE(400, "400_7", "Invalid Report Target Type"),
    MANDATORY_ETC_REASON(400, "400_8", "Mandatory Etc Reason"),
    DUPLICATE_REPORT_EXCEPTION(400, "400_9","Duplicate Report Exception"),
    ACCESS_DENIED_EXCEPTION(401, "401", "401 UnAuthorized"),
    NOT_ALLOW_WRITE_EXCEPTION(401, "401_1", "Not Allow"),
    NOT_ALLOW_RESIGN_EXCEPTION(401, "401_2", "Already resigned user."),
    NOT_ALLOW_OTHER_FORMATS(401, "401_3", "Not Allow Format"),
    DUPLICATED_EXCEPTION(401, "401_4", "Duplicated data"),
    NOT_ALLOW_NO_ACCESS(401, "401_5", "Don't have access"),
    FORBIDDEN_EXCEPTION(403, "403", "403 Forbidden"),
    NOT_FOUND_EXCEPTION(404, "404", "404 Not Found"),
    CONFLICT_EXCEPTION(409, "409", "409 Conflict"),
    KAKAO_ACCESS_TOKEN_FAILED(401, "K4001", "Failed to get access token!"),
    KAKAO_USER_INFO_FAILED(401, "K4002", "Failed to get user info!"),
    INVALID_TOKEN_EXCEPTION(401, "4001", "Invalid JWT token"),
    INVALID_IMAGE_EXCEPTION(401, "4001_1", "Invalid S3 Image URL"),
    JWT_TOKEN_EXPIRED(401, "4002", "JWT token has expired"),
    UNSUPPORTED_JWT_TOKEN(401, "4003", "JWT token is unsupported"),
    EMPTY_JWT_CLAIMS( 401, "4004", "JWT claims string is empty"),
    JWT_SIGNATURE_MISMATCH(401, "4005", "JWT signature does not match"),
    JWT_COMPLEX_ERROR(401, "4006", "JWT Complex error"),
    JWT_NOT_ALLOW_REQUEST(401, "4007", "JWT not allow request"),
    JWT_BLACKLIST(401, "4008", "This token is already blacklisted or is not present.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
