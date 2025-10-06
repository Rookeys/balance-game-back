package com.games.balancegameback.infra.repository.user.impl;

import com.games.balancegameback.domain.user.Follow;
import com.games.balancegameback.infra.entity.FollowEntity;
import com.games.balancegameback.infra.repository.user.FollowJpaRepository;
import com.games.balancegameback.service.user.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final FollowJpaRepository followJpaRepository;

    @Override
    public void save(Follow follow) {
        FollowEntity followEntity = FollowEntity.from(follow);
        followJpaRepository.save(followEntity);
    }

    @Override
    public List<Follow> findByFollowingUid(String followingUid) {
        return followJpaRepository.findByFollowingUid(followingUid)
                .stream()
                .map(FollowEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Follow> findByFollowerUid(String followerUid) {
        return followJpaRepository.findByFollowerUid(followerUid)
                .stream()
                .map(FollowEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByFollowerUidAndFollowingUid(String followerUid, String followingUid) {
        followJpaRepository.deleteByFollowerUidAndFollowingUid(followerUid, followingUid);
    }

    @Override
    public boolean existsByFollowerUidAndFollowingUid(String followerUid, String followingUid) {
        return followJpaRepository.existsByFollowerUidAndFollowingUid(followerUid, followingUid);
    }

    @Override
    public long countByFollowingUid(String followingUid) {
        return followJpaRepository.countByFollowingUid(followingUid);
    }

    @Override
    public long countByFollowerUid(String followerUid) {
        return followJpaRepository.countByFollowerUid(followerUid);
    }
}
