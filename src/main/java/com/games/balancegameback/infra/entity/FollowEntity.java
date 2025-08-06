package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.Follow;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_uid", "following_uid"}))
public class FollowEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_uid", nullable = false)
    private String followerUid; // 팔로워 유저 UID

    @Column(name = "following_uid", nullable = false)
    private String followingUid; // 팔로잉 유저 UID

    public static FollowEntity from(Follow follow) {
        FollowEntity entity = new FollowEntity();
        entity.id = follow.getId();
        entity.followerUid = follow.getFollower();
        entity.followingUid = follow.getFollowing();
        return entity;
    }

    public Follow toModel() {
        return Follow.builder()
                .id(id)
                .follower(followerUid)
                .following(followingUid)
                .build();
    }
}
