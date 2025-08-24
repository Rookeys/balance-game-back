package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.RecentPlay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "recent_plays",
        indexes = {
                @Index(name = "idx_recent_plays_user_time", columnList = "user_uid, created_date DESC"),
                @Index(name = "idx_recent_plays_user_game", columnList = "user_uid, game_id, game_resources_id", unique = true)
        })
public class RecentPlayEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 50)
    private String userUid;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "game_resources_id", nullable = false)
    private Long resourceId;

    public static RecentPlayEntity from(RecentPlay recentPlay) {
        RecentPlayEntity playEntity = new RecentPlayEntity();
        playEntity.id = recentPlay.getId();
        playEntity.userUid = recentPlay.getUserUid();
        playEntity.gameId = recentPlay.getGameId();
        playEntity.resourceId = recentPlay.getResourceId();

        return playEntity;
    }

    public RecentPlay toModel() {
        return RecentPlay.builder()
                .id(id)
                .userUid(userUid)
                .gameId(gameId)
                .resourceId(resourceId)
                .build();
    }
}
