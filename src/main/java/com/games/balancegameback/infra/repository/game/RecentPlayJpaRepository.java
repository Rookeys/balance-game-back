package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.RecentPlayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentPlayJpaRepository extends JpaRepository<RecentPlayEntity, Long> {

    Optional<RecentPlayEntity> findByUserUidAndGameId(String userUid, Long gameId);

    long countByUserUid(String userUid);

    List<RecentPlayEntity> findByUserUidOrderByUpdatedDateDesc(String userUid);

    @Query("SELECT rp FROM RecentPlayEntity rp WHERE rp.userUid = :userUid ORDER BY rp.updatedDate ASC LIMIT 1")
    Optional<RecentPlayEntity> findOldestByUserUid(@Param("userUid") String userUid);
}
