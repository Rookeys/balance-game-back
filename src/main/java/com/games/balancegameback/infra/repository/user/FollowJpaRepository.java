package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.FollowEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowJpaRepository extends JpaRepository<FollowEntity, Long> {

    List<FollowEntity> findByFollowerUid(String followerUid);

    List<FollowEntity> findByFollowingUid(String followingUid);

    /**
     * 팔로워 목록 조회 (페이징)
     * cursorId가 없으면 최신순으로, 있으면 해당 ID보다 작은 것부터 조회
     */
    @Query("SELECT f FROM FollowEntity f " +
           "WHERE f.followingUid = :followingUid " +
           "AND (:cursorId IS NULL OR f.id < :cursorId) " +
           "ORDER BY f.id DESC")
    List<FollowEntity> findByFollowingUidWithPaging(
            @Param("followingUid") String followingUid,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    /**
     * 팔로잉 목록 조회 (페이징)
     * cursorId가 없으면 최신순으로, 있으면 해당 ID보다 작은 것부터 조회
     */
    @Query("SELECT f FROM FollowEntity f " +
           "WHERE f.followerUid = :followerUid " +
           "AND (:cursorId IS NULL OR f.id < :cursorId) " +
           "ORDER BY f.id DESC")
    List<FollowEntity> findByFollowerUidWithPaging(
            @Param("followerUid") String followerUid,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    void deleteByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    boolean existsByFollowerUidAndFollowingUid(String followerUid, String followingUid);

    long countByFollowerUid(String followerUid);

    long countByFollowingUid(String followingUid);

    @Query("SELECT f.followingUid FROM FollowEntity f WHERE f.followerUid = :followerUid")
    List<String> findFollowingUidsByFollowerUid(@Param("followerUid") String followerUid);
}
