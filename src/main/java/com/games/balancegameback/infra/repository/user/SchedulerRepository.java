package com.games.balancegameback.infra.repository.user;

import com.games.balancegameback.infra.entity.GamesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import com.games.balancegameback.infra.repository.game.*;
import com.games.balancegameback.infra.repository.media.ImageJpaRepository;
import com.games.balancegameback.infra.repository.media.LinkJpaRepository;
import com.games.balancegameback.infra.repository.media.MediaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulerRepository {

    private final UserJpaRepository usersRepository;
    private final GameResultCommentJpaRepository gameResultCommentsRepository;
    private final GameCommentLikesJpaRepository gameCommentLikesRepository;
    private final GameResourceCommentJpaRepository gameResourceCommentsRepository;
    private final GameReportJpaRepository gameReportRepository;
    private final ImageJpaRepository imageRepository;
    private final LinkJpaRepository linkRepository;
    private final MediaJpaRepository mediaRepository;
    private final GameJpaRepository gameRepository;

    @Transactional
    public void deleteOldDeletedUsers() {
        OffsetDateTime thresholdDate = OffsetDateTime.now().minusDays(3);

        List<UsersEntity> targets = usersRepository.findAllByIsDeletedTrueAndCreatedDateBefore(thresholdDate);

        for (UsersEntity user : targets) {
            String uid = user.getId();

            List<String> gameIds = gameRepository.findByUserId(uid)
                    .stream()
                    .map(GamesEntity::getId)
                    .collect(Collectors.toList());

            gameCommentLikesRepository.deleteByUserId(uid);
            gameResourceCommentsRepository.deleteByUserId(uid);
            gameReportRepository.deleteByReporterId(uid);

            if (!gameIds.isEmpty()) {
                gameResultCommentsRepository.deleteByGameIdIn(gameIds);
            }

            imageRepository.deleteByUserId(uid);
            linkRepository.deleteByUserId(uid);
            mediaRepository.deleteByUserId(uid);
        }

        for (UsersEntity user : targets) {
            String uid = user.getId();

            gameRepository.deleteByUserId(uid);
            usersRepository.delete(user);
        }
    }
}
