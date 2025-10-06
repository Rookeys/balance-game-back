package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Follow;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.service.user.repository.FollowRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserUtils userUtils;
    private final FollowUtils followUtils;

    /**
     * 팔로워 등록
     */
    @Transactional
    public void addFollow(String followingEmail, HttpServletRequest request) {
        Users follower = userUtils.findUserByToken(request);
        String followingUid = followUtils.getUserUidByEmail(followingEmail);

        if (follower.getUid().equals(followingUid)) {
            throw new BadRequestException("자신을 팔로우할 수 없습니다.", ErrorCode.NOT_FOLLOW_MYSELF);
        }

        boolean alreadyFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                follower.getUid(), followingUid);
        if (alreadyFollowing) {
            throw new BadRequestException("이미 팔로우 중입니다.", ErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(follower.getUid())
                .following(followingUid)
                .build();

        followRepository.save(follow);
    }

    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Follow> getFollowers(String email) {
        String uid = followUtils.getUserUidByEmail(email);
        return followRepository.findByFollowingUid(uid);
    }

    /**
     * 팔로잉 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Follow> getFollowings(String email) {
        String uid = followUtils.getUserUidByEmail(email);
        return followRepository.findByFollowerUid(uid);
    }

    /**
     * 팔로워 취소
     */
    @Transactional
    public void removeFollower(String followerEmail, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        String followerUid = followUtils.getUserUidByEmail(followerEmail);

        boolean isFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                followerUid, users.getUid());
        if (!isFollowing) {
            throw new NotFoundException("팔로우 관계가 없습니다.", ErrorCode.NOT_FOUND_FOLLOWING);
        }

        followRepository.deleteByFollowerUidAndFollowingUid(followerUid, users.getUid());
    }

    /**
     * 팔로잉 취소
     */
    @Transactional
    public void removeFollowing(String followingEmail, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        String followingUid = followUtils.getUserUidByEmail(followingEmail);

        boolean isFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                users.getUid(), followingUid);
        if (!isFollowing) {
            throw new NotFoundException("팔로우 관계가 없습니다.", ErrorCode.NOT_FOUND_FOLLOWING);
        }

        followRepository.deleteByFollowerUidAndFollowingUid(users.getUid(), followingUid);
    }

    /**
     * 팔로우 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(String followingEmail, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        String followingUid = followUtils.getUserUidByEmail(followingEmail);

        return followRepository.existsByFollowerUidAndFollowingUid(users.getUid(), followingUid);
    }

    /**
     * 팔로워 수 조회
     */
    @Transactional(readOnly = true)
    public long getFollowerCount(String email) {
        String uid = followUtils.getUserUidByEmail(email);
        return followRepository.countByFollowingUid(uid);
    }

    /**
     * 팔로잉 수 조회
     */
    @Transactional(readOnly = true)
    public long getFollowingCount(String email) {
        String uid = followUtils.getUserUidByEmail(email);
        return followRepository.countByFollowerUid(uid);
    }
}