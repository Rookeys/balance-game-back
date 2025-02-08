package com.games.balancegameback.core.utils;

import java.util.List;

public class PaginationUtils {

    /**
     * 다음 페이지가 있는지 확인하는 메서드.
     *
     * @param content  페이징된 리스트
     * @param pageSize 요청된 페이지 크기
     * @return 다음 페이지가 존재하면 true, 없으면 false
     */
    public static <T> boolean hasNextPage(List<T> content, int pageSize) {
        return content.size() > pageSize;
    }

    /**
     * 다음 페이지가 있는 경우, 마지막 요소를 제거하여 페이징된 리스트 크기를 조정하는 메서드.
     *
     * @param content  페이징된 리스트
     * @param pageSize 요청된 페이지 크기
     */
    public static <T> void removeLastIfHasNext(List<T> content, int pageSize) {
        if (hasNextPage(content, pageSize)) {
            content.removeLast();
        }
    }
}

