package com.games.balancegameback.core.utils;

import java.util.List;
import java.util.Set;

public class ForbiddenWordUtils {

    // 기본 금칙어 리스트
    public static final Set<String> FORBIDDEN_WORDS = Set.of(
            // 욕설
            "시발", "씨발", "씨팔", "좆", "염병", "썅", "개새끼", "븅신", "병신", "병쉰", "지랄", "지럴", "미친놈", "ㅅㅂ", "ㅄ", "ㅂㅅ",

            // 성적 비하
            "보지", "자지", "꼬추", "딸딸이", "떡", "강간", "섹스", "성인용품",

            // 차별 혐오
            "틀딱", "꼰대", "홍어", "똥꼬충", "급식충", "장님", "벙어리", "장애",

            // 불법 광고
            "카지노", "토토", "먹튀", "야동", "성매매", "룸살롱", "원나잇", "대출", "카드 발급", "리딩방",

            // 기타 비속어
            "좆망", "개같네", "대가리", "싸가지", "애미", "애비"
    );

    private ForbiddenWordUtils() {
        // 인스턴스 생성 방지
    }

    public static boolean containsForbiddenWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String forbiddenWord : FORBIDDEN_WORDS) {
            if (text.contains(forbiddenWord)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> findForbiddenWords(String text) {
        return FORBIDDEN_WORDS.stream()
                .filter(text::contains)
                .toList();
    }
}
