package com.games.balancegameback.domain.game;

import lombok.Builder;
import lombok.Data;

@Data
public class Follow {

    private Long id;
    private String follower;
    private String following;

    @Builder
    public Follow(Long id, String follower, String following) {
        this.id = id;
        this.follower = follower;
        this.following = following;
    }
}
