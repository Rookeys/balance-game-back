package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.RecentPlay;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.RecentPlayListResponse;
import com.games.balancegameback.service.game.repository.RecentPlayRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecentPlayService {

    private final RecentPlayRepository recentPlayRepository;
    private final UserUtils userUtils;
    private static final int MAX_RECENT_PLAYS = 50;

    @Transactional
    public Long addRecentPlay(Long gameId, Long resourceId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        if (users == null) {
            throw new UnAuthorizedException("토큰값이 유효하지 않습니다.", ErrorCode.INVALID_TOKEN_EXCEPTION);
        }

        // 이미 플레이한 게임인지 확인
        Optional<RecentPlay> existingPlay = recentPlayRepository.findByUserUidAndGameId(users.getUid(), gameId);

        if (existingPlay.isPresent()) {
            // 존재한다면 updated_date 만 갱신
            return recentPlayRepository.updateRecentPlay(existingPlay.get());
        } else {
            RecentPlay recentPlay = RecentPlay.create(users.getUid(), gameId, resourceId);

            long currentCount = recentPlayRepository.countByUserUid(users.getUid());

            if (currentCount >= MAX_RECENT_PLAYS) {
                // 가장 오래된 기록 삭제
                Optional<RecentPlay> oldestPlay = recentPlayRepository.findOldestByUserUid(users.getUid());
                oldestPlay.ifPresent(recentPlayRepository::delete);
            }

            return recentPlayRepository.save(recentPlay);
        }
    }

    @Transactional(readOnly = true)
    public CustomPageImpl<RecentPlayListResponse> getRecentPlays(Long cursorId, Pageable pageable, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        if (users == null) {
            throw new UnAuthorizedException("토큰값이 유효하지 않습니다.", ErrorCode.INVALID_TOKEN_EXCEPTION);
        }

        return recentPlayRepository.getRecentPlayList(cursorId, pageable, users);
    }

    @Transactional
    public void deleteRecentPlay(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        if (users == null) {
            throw new UnAuthorizedException("토큰값이 유효하지 않습니다.", ErrorCode.INVALID_TOKEN_EXCEPTION);
        }

        Optional<RecentPlay> recentPlay = recentPlayRepository.findByUserUidAndGameId(users.getUid(), roomId);

        recentPlay.ifPresent(recentPlayRepository::delete);
    }
}

