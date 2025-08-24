package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.RecentPlay;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.RecentPlayListResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RecentPlayRepository {

    Long save(RecentPlay recentPlay);

    Long updateRecentPlay(RecentPlay recentPlay);

    void delete(RecentPlay recentPlay);

    Optional<RecentPlay> findByUserUidAndGameId(String userUid, Long gameId);

    long countByUserUid(String userUid);

    CustomPageImpl<RecentPlayListResponse> getRecentPlayList(Long cursorId, Pageable pageable, Users user);

    Optional<RecentPlay> findOldestByUserUid(String userUid);
}
