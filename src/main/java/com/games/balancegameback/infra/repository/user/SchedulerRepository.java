package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.GamesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.infra.repository.game.*;
import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SchedulerRepository {

    private final UserJpaRepository usersRepository;
    private final GameCommentLikesJpaRepository gameCommentLikesRepository;
    private final GameReportJpaRepository gameReportRepository;
    private final ImageJpaRepository imageRepository;
    private final LinkJpaRepository linkRepository;
    private final MediaJpaRepository mediaRepository;
    private final GameJpaRepository gameRepository;

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

        gameCommentLikesRepository.deleteByUsersUid(uid);
        gameReportRepository.deleteByReporterUid(uid);

        imageRepository.deleteByUsersUid(uid);
        linkRepository.deleteByUsersUid(uid);
        mediaRepository.deleteByUsersUid(uid);

        if (!userCreatedGameIds.isEmpty()) {
            gameRepository.deleteByUsersUid(uid);
        }

        // 사용자 정보 익명화
        anonymizeUserInfo(user);
    }

    /**
     * 사용자 개인정보 익명화
     * 개인을 식별할 수 있는 정보들 익명화
     */
    private void anonymizeUserInfo(UsersEntity user) {
        usersRepository.anonymizeUserPersonalInfo(
                user.getUid(),
                "DELETED_USER_" + user.getUid().substring(user.getUid().length() - 15) // UID 뒷 15자리로 구분
        );
    }
}
