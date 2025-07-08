package com.games.balancegameback.infra.repository.user;

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
    private final LinkJpaRepository linkRepository;
    private final MediaJpaRepository mediaRepository;
    private final GameJpaRepository gameRepository;

    private final UserMediaCleanupService mediaCleanupService;

    @Transactional
    public void deleteOldDeletedUsers() {
        OffsetDateTime thresholdDate = OffsetDateTime.now().minusDays(3);

        List<UsersEntity> targets = usersRepository.findAllByIsDeletedTrueAndCreatedDateBefore(thresholdDate);

        if (targets.isEmpty()) {
            log.info("탈퇴 처리할 사용자가 없습니다.");
            return;
        }

        log.info("{}명의 탈퇴 사용자 처리를 시작합니다.", targets.size());

        for (UsersEntity user : targets) {
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
            gameRepository.deleteByUsersUid(uid);
            log.debug("사용자 {}가 생성한 {}개 게임 삭제 완료", uid, userCreatedGameIds.size());
        }

        anonymizeUserInfo(user);

        log.debug("사용자 {} 개인정보 익명화 완료", uid);
    }

    /**
     * 사용자 개인정보 익명화
     * UID는 유지하되, 개인을 식별할 수 있는 정보(닉네임, 이메일)를 익명화
     */
    private void anonymizeUserInfo(UsersEntity user) {
        String anonymousNickname = UsersEntity.createAnonymousNickname(user.getUid());
        String anonymousEmail = UsersEntity.createAnonymousEmail(user.getEmail());

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
