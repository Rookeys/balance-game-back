package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.Report;
import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.report.GameCommentReportRequest;
import com.games.balancegameback.dto.game.report.GameReportRequest;
import com.games.balancegameback.dto.user.UserReportRequest;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameReportJpaRepository;
import com.games.balancegameback.service.game.repository.GameReportRepository;
import com.games.balancegameback.service.user.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
@RequiredArgsConstructor
public class GameReportRepositoryImpl implements GameReportRepository {

    private final GameReportJpaRepository reportRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    private final QGamesEntity games = QGamesEntity.gamesEntity;
    private final QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
    private final QGameResourceCommentsEntity resourceComments = QGameResourceCommentsEntity.gameResourceCommentsEntity;
    private final QGameResultCommentsEntity resultComments = QGameResultCommentsEntity.gameResultCommentsEntity;

    @Override
    public boolean existsByGameId(Long gameId) {
        Integer result = queryFactory
                .selectOne()
                .from(games)
                .where(games.id.eq(gameId))
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsByGameIdAndResourceId(Long gameId, Long resourceId) {
        Integer result = queryFactory
                .selectOne()
                .from(resources)
                .where(resources.games.id.eq(gameId)
                        .and(resources.id.eq(resourceId)))
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsByGameIdAndComments(Long gameId, GameCommentReportRequest commentReportRequest) {
        boolean exists;

        switch (commentReportRequest.getTargetType()) {
            case RESOURCE_COMMENT -> {
                exists = queryFactory
                        .selectOne()
                        .from(resourceComments)
                        .where(
                                resourceComments.gameResources.games.id.eq(gameId),
                                resourceComments.id.eq(commentReportRequest.getTargetId()),
                                resourceComments.isDeleted.isFalse()
                        )
                        .fetchFirst() != null;
            }
            case RESULT_COMMENT -> {
                exists = queryFactory
                        .selectOne()
                        .from(resultComments)
                        .where(
                                resultComments.games.id.eq(gameId),
                                resultComments.id.eq(commentReportRequest.getTargetId())
                        )
                        .fetchFirst() != null;
            }
            default -> throw new NotFoundException("잘못된 신고 유형입니다.", ErrorCode.RUNTIME_EXCEPTION);
        }

        return exists;
    }

    @Override
    public void saveGameReport(Long gameId, Users users, GameReportRequest gameReportRequest) {
        Report report = Report.builder()
                .targetType(gameReportRequest.getTargetType())
                .targetId(gameId)
                .reporter(users)
                .reasons(new ArrayList<>(gameReportRequest.getReasons()))
                .etcReason(gameReportRequest.getEtcReason())
                .build();

        reportRepository.save(ReportEntity.from(report));
    }

    @Override
    public void saveGameResourceReport(Long gameId, Long resourceId, Users users, GameReportRequest gameReportRequest) {
        Report report = Report.builder()
                .targetType(gameReportRequest.getTargetType())
                .targetId(resourceId)
                .reporter(users)
                .reasons(new ArrayList<>(gameReportRequest.getReasons()))
                .etcReason(gameReportRequest.getEtcReason())
                .build();

        reportRepository.save(ReportEntity.from(report));
    }

    @Override
    public void saveCommentReport(Long gameId, Users users, GameCommentReportRequest commentReportRequest) {
        Report report = Report.builder()
                .targetType(commentReportRequest.getTargetType())
                .targetId(commentReportRequest.getTargetId())
                .reporter(users)
                .reasons(new ArrayList<>(commentReportRequest.getReasons()))
                .etcReason(commentReportRequest.getEtcReason())
                .build();

        reportRepository.save(ReportEntity.from(report));
    }

    @Override
    public void saveUserReport(Users users, UserReportRequest userReportRequest) {
        Users user = userRepository.findByNickname(userReportRequest.getNickname());
        Report report = Report.builder()
                .targetType(userReportRequest.getTargetType())
                .targetUid(user.getUid())
                .reporter(users)
                .reasons(new ArrayList<>(userReportRequest.getReasons()))
                .etcReason(userReportRequest.getEtcReason())
                .build();

        reportRepository.save(ReportEntity.from(report));
    }

    @Override
    public boolean existsGameReport(Long gameId, String uid) {
        QReportEntity report = QReportEntity.reportEntity;

        Integer result = queryFactory
                .selectOne()
                .from(report)
                .where(
                        report.targetType.eq(ReportTargetType.GAME),
                        report.targetId.eq(gameId),
                        report.reporter.uid.eq(uid)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsGameResourceReport(Long gameId, Long resourceId, String uid) {
        QReportEntity report = QReportEntity.reportEntity;

        Integer result = queryFactory
                .selectOne()
                .from(report)
                .where(
                        report.targetType.eq(ReportTargetType.RESOURCE),
                        report.targetId.eq(resourceId),
                        report.reporter.uid.eq(uid)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsGameCommentReport(Long gameId, GameCommentReportRequest commentReportRequest, String uid) {
        QReportEntity report = QReportEntity.reportEntity;
        Long commentId = commentReportRequest.getTargetId();

        Integer result = queryFactory
                .selectOne()
                .from(report)
                .where(
                        report.targetType.eq(commentReportRequest.getTargetType()),
                        report.targetId.eq(commentId),
                        report.reporter.uid.eq(uid)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public boolean existsUserReport(String nickname, String uid) {
        QReportEntity report = QReportEntity.reportEntity;
        Users target = userRepository.findByNickname(nickname);

        Integer result = queryFactory
                .selectOne()
                .from(report)
                .where(
                        report.targetType.eq(ReportTargetType.USER),
                        report.targetUid.eq(target.getUid()),
                        report.reporter.uid.eq(uid)
                )
                .fetchFirst();

        return result != null;
    }
}


