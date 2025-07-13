package com.games.balancegameback.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class UserAnonymizationUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern DELETED_EMAIL_PATTERN = Pattern.compile("^DELETED_USER_(\\d{17})_(.+)$");
    private static final Pattern DELETED_NICKNAME_PATTERN = Pattern.compile("^탈퇴한 사용자_(\\d{17})$");

    /**
     * 타임스탬프 + 랜덤 조합
     */
    public static String generateTimestampBasedId() {
        long timestamp = Instant.now().toEpochMilli();
        int random = SECURE_RANDOM.nextInt(10000);
        return String.format("%d%04d", timestamp, random);
    }

    /**
     * 이메일 익명화
     * 형태: DELETED_USER_16917234567891234_unknown@deleted.user
     */
    public static String anonymizeEmail(String originalEmail) {
        if (originalEmail == null || originalEmail.trim().isEmpty()) {
            return "DELETED_USER_" + generateTimestampBasedId() + "_unknown@deleted.user";
        }

        // 이미 익명화된 이메일인지 확인
        if (isAnonymizedEmail(originalEmail)) {
            log.warn("이미 익명화된 이메일입니다: {}", originalEmail);
            return originalEmail;
        }

        return "DELETED_USER_" + generateTimestampBasedId() + "_" + originalEmail;
    }

    /**
     * 닉네임 익명화
     * 형태: 탈퇴한 사용자_16917234567891234
     */
    public static String anonymizeNickname(String uid) {
        return "탈퇴한 사용자_" + generateTimestampBasedId();
    }

    /**
     * 익명화된 이메일에서 원본 이메일 추출
     */
    public static String extractOriginalEmail(String anonymizedEmail) {
        if (anonymizedEmail == null) {
            return null;
        }

        var matcher = DELETED_EMAIL_PATTERN.matcher(anonymizedEmail);
        if (matcher.matches()) {
            return matcher.group(2);
        }

        return anonymizedEmail;
    }

    /**
     * 타임스탬프 추출
     */
    public static Long extractDeletionTimestamp(String anonymizedData) {
        if (anonymizedData == null) {
            return null;
        }

        // 이메일에서 타임스탬프 추출
        var emailMatcher = DELETED_EMAIL_PATTERN.matcher(anonymizedData);
        if (emailMatcher.matches()) {
            String timestampStr = emailMatcher.group(1).substring(0, 13);
            return Long.parseLong(timestampStr);
        }

        // 닉네임에서 타임스탬프 추출
        var nicknameMatcher = DELETED_NICKNAME_PATTERN.matcher(anonymizedData);
        if (nicknameMatcher.matches()) {
            String timestampStr = nicknameMatcher.group(1).substring(0, 13);
            return Long.parseLong(timestampStr);
        }

        return null;
    }

    /**
     * 익명화된 이메일인지 확인
     */
    public static boolean isAnonymizedEmail(String email) {
        return email != null && DELETED_EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 익명화된 닉네임인지 확인
     */
    public static boolean isAnonymizedNickname(String nickname) {
        return nickname != null && DELETED_NICKNAME_PATTERN.matcher(nickname).matches();
    }

    /**
     * 사용자가 익명화되었는지 확인 (닉네임 또는 이메일 기준)
     */
    public static boolean isUserAnonymized(String nickname, String email) {
        return isAnonymizedNickname(nickname) || isAnonymizedEmail(email);
    }
}
