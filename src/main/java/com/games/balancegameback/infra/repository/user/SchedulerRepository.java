package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.core.utils.UserAnonymizationUtils;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.*;
import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import com.games.balancegameback.service.media.impl.UserMediaCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerRepository {

    private final UserJpaRepository usersRepository;
    private final GameResultCommentJpaRepository gameResultCommentsRepository;
    private final GameResourceCommentJpaRepository gameResourceCommentsRepository;
    private final LinkJpaRepository linkRepository;
    private final MediaJpaRepository mediaRepository;
    private final GameJpaRepository gameRepository;

    private final UserMediaCleanupService mediaCleanupService;

    @Transactional
    public void deleteOldDeletedUsers() {
        OffsetDateTime thresholdDate = OffsetDateTime.now().minusDays(3);

        List<UsersEntity> targets = usersRepository.findAllByIsDeletedTrueAndUpdatedDateBefore(thresholdDate);

        List<UsersEntity> needProcessingUsers = targets.stream()
                .filter(user -> !UserAnonymizationUtils.isUserAnonymized(user.getNickname(), user.getEmail()))
                .toList();

        if (needProcessingUsers.isEmpty()) {
            log.info("탈퇴 처리할 사용자가 없습니다.");
            return;
        }

        log.info("{}명의 탈퇴 사용자 처리를 시작합니다.", needProcessingUsers.size());

        for (UsersEntity user : needProcessingUsers) {
            String uid = user.getUid();

            try {
                processDeletedUser(user);
                log.info("사용자 {} 처리 완료", uid);
            } catch (Exception e) {
                log.error("사용자 {} 처리 중 오류 발생: {}", uid, e.getMessage(), e);
            }
        }

        log.info("탈퇴 사용자 처리가 완료되었습니다.");
    }

    private void processDeletedUser(UsersEntity user) {
        String uid = user.getUid();

        // 더블 체크
        if (UserAnonymizationUtils.isUserAnonymized(user.getNickname(), user.getEmail())) {
            log.warn("사용자 {}는 이미 익명화되어 있습니다. 처리를 건너뜁니다.", uid);
            return;
        }

        List<Long> userCreatedGameIds = gameRepository.findByUsersUid(uid)
                .stream()
                .map(GamesEntity::getId)
                .toList();

        log.debug("사용자 {}가 생성한 게임 {}개 발견", uid, userCreatedGameIds.size());

        if (!userCreatedGameIds.isEmpty()) {
            for (Long gameId : userCreatedGameIds) {
                mediaCleanupService.cleanupGameMediaFiles(gameId);
            }
        }

        mediaCleanupService.cleanupUserMediaFiles(uid);

        linkRepository.deleteByUsersUid(uid);
        mediaRepository.deleteByUsersUid(uid);

        if (!userCreatedGameIds.isEmpty()) {
            deleteGameRelatedComments(userCreatedGameIds);
            gameRepository.deleteByUsersUid(uid);
            log.debug("사용자 {}가 생성한 {}개 게임 삭제 완료", uid, userCreatedGameIds.size());
        }

        anonymizeUserInfo(user);

        log.debug("사용자 {} 개인정보 익명화 완료", uid);
    }

    /**
     * 게임 관련 모든 댓글 삭제 (FK 제약조건 해결)
     */
    private void deleteGameRelatedComments(List<Long> gameIds) {
        for (Long gameId : gameIds) {
            try {
                // 리소스 댓글 삭제
                gameResourceCommentsRepository.deleteByGamesId(gameId);
                // 결과 댓글 삭제
                gameResultCommentsRepository.deleteByGamesId(gameId);

                log.debug("게임 {}의 모든 댓글 삭제 완료", gameId);

            } catch (Exception e) {
                log.error("게임 {}의 댓글 삭제 중 오류: {}", gameId, e.getMessage());
            }
        }
    }

    /**
     * 사용자 개인정보 익명화
     * UID는 유지하되, 개인을 식별할 수 있는 정보(닉네임, 이메일)를 익명화
     */
    private void anonymizeUserInfo(UsersEntity user) {
        String anonymousNickname = UserAnonymizationUtils.anonymizeNickname(user.getUid());
        String anonymousEmail = UserAnonymizationUtils.anonymizeEmail(user.getEmail());

        usersRepository.anonymizeUserPersonalInfo(
            user.getUid(),
            anonymousNickname,
            anonymousEmail
        );

        log.debug("사용자 {} 익명화: {} -> {}, {} -> {}",
                 user.getUid(),
                 user.getNickname(), anonymousNickname,
                 user.getEmail(), anonymousEmail);
    }
}
