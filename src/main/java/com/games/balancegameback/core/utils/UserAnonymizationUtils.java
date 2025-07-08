package com.games.balancegameback.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 사용자 데이터 익명화 유틸리티
 */
@Slf4j
@UtilityClass
public class UserAnonymizationUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern DELETED_EMAIL_PATTERN = Pattern.compile("^DELETED_USER_\\d{6}_(.+)$");
    private static final Pattern DELETED_NICKNAME_PATTERN = Pattern.compile("^탈퇴한 사용자_(.+)$");

    /**
     * 안전한 6자리 난수 생성
     */
    public static String generateSecureRandomNumber() {
        return String.format("%06d", SECURE_RANDOM.nextInt(900000) + 100000);
    }

    /**
     * 이메일 익명화
     * 형태: DELETED_USER_123456_원본이메일
     */
    public static String anonymizeEmail(String originalEmail) {
        if (originalEmail == null || originalEmail.trim().isEmpty()) {
            return "DELETED_USER_" + generateSecureRandomNumber() + "_unknown@deleted.user";
        }

        // 이미 익명화된 이메일인지 확인
        if (isAnonymizedEmail(originalEmail)) {
            log.warn("이미 익명화된 이메일입니다: {}", originalEmail);
            return originalEmail;
        }

        return "DELETED_USER_" + generateSecureRandomNumber() + "_" + originalEmail;
    }

    /**
     * 닉네임 익명화
     */
    public static String anonymizeNickname(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return "탈퇴한 사용자_UNKNOWN";
        }

        String suffix = uid.length() >= 8 ?
            uid.substring(uid.length() - 8) :
            uid + "_" + generateSecureRandomNumber().substring(0, 4);

        return "탈퇴한 사용자_" + suffix;
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
            return matcher.group(1);
        }

        return anonymizedEmail; // 익명화되지 않은 경우 그대로 반환
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

    /**
     * 익명화 정보 통계
     */
    public static AnonymizationInfo analyzeAnonymization(String nickname, String email) {
        return AnonymizationInfo.builder()
                .isNicknameAnonymized(isAnonymizedNickname(nickname))
                .isEmailAnonymized(isAnonymizedEmail(email))
                .originalEmail(extractOriginalEmail(email))
                .build();
    }

    /**
     * 익명화 정보 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class AnonymizationInfo {
        private boolean isNicknameAnonymized;
        private boolean isEmailAnonymized;
        private String originalEmail;

        public boolean isFullyAnonymized() {
            return isNicknameAnonymized && isEmailAnonymized;
        }
    }
}
