package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowJpaRepository extends JpaRepository<FollowEntity, Long> {

    List<FollowEntity> findByFollowerUid(String followerUid);

    List<FollowEntity> findByFollowingUid(String followingUid);

    void deleteByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    boolean existsByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    long countByFollowerUid(String followerUid);

    long countByFollowingUid(String followingUid);

    @Query("SELECT f.followingUid FROM FollowEntity f WHERE f.followerUid = :followerUid")
    List<String> findFollowingUidsByFollowerUid(@Param("followerUid") String followerUid);
}
