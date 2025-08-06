package com.games.balancegameback.infra.repository.game.common;

/**
 * 커서 페이징이 가능한 객체를 위한 인터페이스
 */
public interface CursorIdentifiable {
    /**
     * 커서 값을 반환
     * @return CursorId
     */
    Long getCursorValue();
}
