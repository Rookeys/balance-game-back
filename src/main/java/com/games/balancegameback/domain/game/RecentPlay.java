package com.games.balancegameback.domain.game;

import lombok.Builder;
import lombok.Data;

@Data
public class RecentPlay {

    private Long id;
    private String userUid;
    private Long gameId;
    private Long resourceId;

    @Builder
    public RecentPlay(Long id, String userUid, Long gameId, Long resourceId) {
        this.id = id;
        this.userUid = userUid;
        this.gameId = gameId;
        this.resourceId = resourceId;
    }

    public static RecentPlay create(String userUid, Long gameId, Long resourceId) {
        return RecentPlay.builder()
                .userUid(userUid)
                .gameId(gameId)
                .resourceId(resourceId)
                .build();
    }
}
