package com.games.balancegameback.infra.repository.game.common;

import com.querydsl.core.Tuple;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CommonGameUtils {

    /**
     * Tuple 리스트에서 게임 ID 추출
     */
    public static List<Long> extractGameIds(List<Tuple> tuples) {
        return tuples.stream()
                .map(tuple -> tuple.get(GameQClasses.games.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Long 값을 int로 변환
     */
    public static int safeIntValue(Long value) {
        return value != null ? Math.toIntExact(value) : 0;
    }

    /**
     * 익명 사용자 정보 처리
     */
    public static String processNickname(String originalNickname, boolean isPrivate) {
        return isPrivate ? GameConstants.ANONYMOUS_NICKNAME : originalNickname;
    }

    /**
     * 익명 사용자 프로필 이미지 처리
     */
    public static String processProfileImage(String originalImage, boolean isPrivate) {
        return isPrivate ? null : originalImage;
    }

    /**
     * 빈 GameBatchData 생성
     */
    public static GameBatchData createEmptyBatchData() {
        return GameBatchData.builder()
                .categoriesMap(Collections.emptyMap())
                .selectionsMap(Collections.emptyMap())
                .playCountsMap(Collections.emptyMap())
                .totalPlayCountsMap(Collections.emptyMap())
                .build();
    }

    private CommonGameUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
