package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Follow;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.FollowCountResponse;
import com.games.balancegameback.dto.user.FollowUserResponse;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.repository.FollowRepository;
import com.games.balancegameback.service.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
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
     * 팔로워 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public CustomPageImpl<FollowUserResponse> getFollowers(String email, Long cursorId, Pageable pageable, HttpServletRequest request) {
        String uid = followUtils.getUserUidByEmail(email);
        
        // size + 1 개를 조회해서 hasNext 판단
        Pageable fetchPageable = org.springframework.data.domain.PageRequest.of(0, pageable.getPageSize() + 1);
        List<Follow> follows = followRepository.findByFollowingUidWithPaging(uid, cursorId, fetchPageable);
        
        boolean hasNext = follows.size() > pageable.getPageSize();
        if (hasNext) {
            follows = follows.subList(0, pageable.getPageSize());
        }
        
        // 현재 로그인한 사용자 확인
        Users currentUser = null;
        try {
            currentUser = userUtils.findUserByToken(request);
        } catch (Exception e) {
            // 로그인하지 않은 경우
        }
        
        if (follows.isEmpty()) {
            long totalCount = followRepository.countByFollowingUid(uid);
            return new CustomPageImpl<>(List.of(), pageable, totalCount, cursorId, false);
        }
        
        // 팔로워들의 UID 목록
        List<String> followerUids = follows.stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
        
        // 팔로워들의 사용자 정보 조회 후 Map으로 변환
        Map<String, Users> followerMap = userRepository.findByUids(followerUids).stream()
                .collect(Collectors.toMap(Users::getUid, user -> user));
        
        Users finalCurrentUser = currentUser;
        List<FollowUserResponse> responses = follows.stream()
                .map(follow -> {
                    Users follower = followerMap.get(follow.getFollower());
                    if (follower == null) {
                        return null;
                    }
                    
                    Images images = imageRepository.findByUsers(follower);
                    
                    // 팔로우 버튼 표시 여부
                    boolean showFollowButton = finalCurrentUser != null && 
                            !finalCurrentUser.getUid().equals(follower.getUid());
                    
                    // 팔로우 여부 확인
                    boolean isFollowing = false;
                    if (showFollowButton) {
                        isFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                                finalCurrentUser.getUid(), 
                                follower.getUid()
                        );
                    }
                    
                    return FollowUserResponse.builder()
                            .followId(follow.getId())
                            .nickname(follower.getNickname())
                            .email(follower.getEmail())
                            .fileUrl(images == null ? null : images.getFileUrl())
                            .isFollowing(isFollowing)
                            .showFollowButton(showFollowButton)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        long totalCount = followRepository.countByFollowingUid(uid);
        return new CustomPageImpl<>(responses, pageable, totalCount, cursorId, hasNext);
    }

    /**
     * 팔로잉 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public CustomPageImpl<FollowUserResponse> getFollowings(String email, Long cursorId, Pageable pageable, HttpServletRequest request) {
        String uid = followUtils.getUserUidByEmail(email);
        
        // size + 1 개를 조회해서 hasNext 판단
        Pageable fetchPageable = org.springframework.data.domain.PageRequest.of(0, pageable.getPageSize() + 1);
        List<Follow> follows = followRepository.findByFollowerUidWithPaging(uid, cursorId, fetchPageable);
        
        boolean hasNext = follows.size() > pageable.getPageSize();
        if (hasNext) {
            follows = follows.subList(0, pageable.getPageSize());
        }
        
        // 현재 로그인한 사용자 확인
        Users currentUser = null;
        try {
            currentUser = userUtils.findUserByToken(request);
        } catch (Exception e) {
            // 로그인하지 않은 경우
        }
        
        if (follows.isEmpty()) {
            long totalCount = followRepository.countByFollowerUid(uid);
            return new CustomPageImpl<>(List.of(), pageable, totalCount, cursorId, false);
        }
        
        // 팔로잉들의 UID 목록
        List<String> followingUids = follows.stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
        
        // 팔로잉들의 사용자 정보 조회 후 Map으로 변환
        Map<String, Users> followingMap = userRepository.findByUids(followingUids).stream()
                .collect(Collectors.toMap(Users::getUid, user -> user));
        
        Users finalCurrentUser = currentUser;
        List<FollowUserResponse> responses = follows.stream()
                .map(follow -> {
                    Users following = followingMap.get(follow.getFollowing());
                    if (following == null) {
                        return null;
                    }
                    
                    Images images = imageRepository.findByUsers(following);
                    
                    // 팔로우 버튼 표시 여부
                    boolean showFollowButton = finalCurrentUser != null && 
                            !finalCurrentUser.getUid().equals(following.getUid());
                    
                    // 팔로우 여부 확인
                    boolean isFollowing = false;
                    if (showFollowButton) {
                        isFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                                finalCurrentUser.getUid(), 
                                following.getUid()
                        );
                    }
                    
                    return FollowUserResponse.builder()
                            .followId(follow.getId())
                            .nickname(following.getNickname())
                            .email(following.getEmail())
                            .fileUrl(images == null ? null : images.getFileUrl())
                            .isFollowing(isFollowing)
                            .showFollowButton(showFollowButton)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        long totalCount = followRepository.countByFollowerUid(uid);
        return new CustomPageImpl<>(responses, pageable, totalCount, cursorId, hasNext);
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
     * 팔로워 / 팔로잉 수 조회
     */
    @Transactional(readOnly = true)
    public FollowCountResponse getFollowCounts(String email) {
        String uid = followUtils.getUserUidByEmail(email);
        
        long followerCount = followRepository.countByFollowingUid(uid);
        long followingCount = followRepository.countByFollowerUid(uid);
        
        return FollowCountResponse.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    /**
     * 추천 프로필 목록 조회
     * 랜덤으로 팔로우하지 않은 사용자를 반환
     */
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getRecommendedProfiles(HttpServletRequest request) {
        // 현재 로그인한 사용자 확인
        Users currentUser = null;
        String currentUserUid = null;
        List<String> followingUids = new ArrayList<>();
        
        try {
            currentUser = userUtils.findUserByToken(request);
            if (currentUser != null) {
                currentUserUid = currentUser.getUid();
                // 팔로우 중인 사용자들의 UID 목록 조회
                followingUids = followRepository.findFollowingUidsByFollowerUid(currentUserUid);
            }
        } catch (Exception e) {
            // 로그인하지 않은 경우
        }

        List<Users> recommendedUsers = userRepository.findRandomUsersExcludingUids(
                currentUserUid, 
                followingUids, 
                6
        );

        Users finalCurrentUser = currentUser;
        return recommendedUsers.stream()
                .map(user -> {
                    // 혹시 모를 자신 제외
                    if (finalCurrentUser != null && user.getUid().equals(finalCurrentUser.getUid())) {
                        return null;
                    }
                    
                    Images images = imageRepository.findByUsers(user);
                    
                    // 팔로우 버튼 표시 여부
                    boolean showFollowButton = finalCurrentUser != null;
                    
                    return FollowUserResponse.builder()
                            .followId(null) // 추천 프로필은 팔로우 관계가 없으므로 null
                            .nickname(user.getNickname())
                            .email(user.getEmail())
                            .fileUrl(images == null ? null : images.getFileUrl())
                            .isFollowing(false)
                            .showFollowButton(showFollowButton)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}