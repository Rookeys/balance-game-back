package com.games.balancegameback.domain.game;

import lombok.Builder;
import lombok.Data;

@Data
public class RecentPlay {

    private Long id;
    private String userUid;
    private Long gameId;

    @Builder
    public RecentPlay(Long id, String userUid, Long gameId) {
        this.id = id;
        this.userUid = userUid;
        this.gameId = gameId;
    }

    public static RecentPlay create(String userUid, Long gameId) {
        return RecentPlay.builder()
                .userUid(userUid)
                .gameId(gameId)
                .build();
    }
}
