package com.games.balancegameback.service.user.repository;

import com.games.balancegameback.domain.user.Follow;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FollowRepository {

    void save(Follow follow);

    List<Follow> findByFollowerUid(String followerUid);

    List<Follow> findByFollowingUid(String followingUid);

    /**
     * 팔로워 목록 조회 (페이징)
     */
    List<Follow> findByFollowingUidWithPaging(String followingUid, Long cursorId, Pageable pageable);

    /**
     * 팔로잉 목록 조회 (페이징)
     */
    List<Follow> findByFollowerUidWithPaging(String followerUid, Long cursorId, Pageable pageable);

    void deleteByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    boolean existsByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    long countByFollowerUid(String followerUid);

    long countByFollowingUid(String followingUid);

    /**
     * 특정 사용자가 팔로우하는 사용자들의 UID 목록 조회
     */
    List<String> findFollowingUidsByFollowerUid(String followerUid);
}
