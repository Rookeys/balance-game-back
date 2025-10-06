package com.games.balancegameback.service.user.repository;

import com.games.balancegameback.domain.user.Follow;

import java.util.List;

public interface FollowRepository {

    void save(Follow follow);

    List<Follow> findByFollowerUid(String followerUid);

    List<Follow> findByFollowingUid(String followingUid);

    void deleteByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    boolean existsByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    long countByFollowerUid(String followerUid);

    long countByFollowingUid(String followingUid);
}
