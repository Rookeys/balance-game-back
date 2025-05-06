package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.report.GameCommentReportRequest;
import com.games.balancegameback.dto.game.report.GameReportRequest;
import com.games.balancegameback.dto.user.UserReportRequest;
import com.games.balancegameback.service.game.repository.GameReportRepository;
import com.games.balancegameback.service.user.UserRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameReportService {

    private final GameReportRepository gameReportRepository;
    private final UserRepository userRepository;
    private final UserUtils userUtils;

    @Transactional
    public void submitGamesReport(Long gameId, GameReportRequest gameReportRequest, HttpServletRequest request) {

        if (!gameReportRequest.getTargetType().equals(ReportTargetType.GAME)) {
            throw new BadRequestException("잘못된 요청입니다.", ErrorCode.INVALID_REPORT_TARGET_TYPE);
        }

        if (gameReportRequest.getReasons().contains("ETC") &&
                (gameReportRequest.getEtcReason() == null || gameReportRequest.getEtcReason().isBlank())) {
            throw new BadRequestException("이유를 적어야 합니다.", ErrorCode.MANDATORY_ETC_REASON);
        }

        if (!gameReportRepository.existsByGameId(gameId)) {
            throw new NotFoundException("해당 게임방은 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("잘못된 유저 정보입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (gameReportRepository.existsGameReport(gameId, users.getUid())) {
            throw new BadRequestException("이미 신고한 게임입니다.", ErrorCode.DUPLICATE_REPORT_EXCEPTION);
        }

        gameReportRepository.saveGameReport(gameId, users, gameReportRequest);
    }

    @Transactional
    public void submitGameResourcesReport(Long gameId, Long resourceId, GameReportRequest gameReportRequest,
                                          HttpServletRequest request) {

        if (!gameReportRequest.getTargetType().equals(ReportTargetType.RESOURCE)) {
            throw new BadRequestException("잘못된 요청입니다.", ErrorCode.INVALID_REPORT_TARGET_TYPE);
        }

        if (gameReportRequest.getReasons().contains("ETC") &&
                (gameReportRequest.getEtcReason() == null || gameReportRequest.getEtcReason().isBlank())) {
            throw new BadRequestException("이유를 적어야 합니다.", ErrorCode.MANDATORY_ETC_REASON);
        }

        if (!gameReportRepository.existsByGameId(gameId)) {
            throw new NotFoundException("해당 게임방은 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        if (!gameReportRepository.existsByGameIdAndResourceId(gameId, resourceId)) {
            throw new NotFoundException("해당 리소스는 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("잘못된 유저 정보입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (gameReportRepository.existsGameResourceReport(gameId, resourceId, users.getUid())) {
            throw new BadRequestException("이미 신고한 리소스입니다.", ErrorCode.DUPLICATE_REPORT_EXCEPTION);
        }

        gameReportRepository.saveGameResourceReport(gameId, resourceId, users, gameReportRequest);
    }

    @Transactional
    public void submitGameCommentsReport(Long gameId, GameCommentReportRequest commentReportRequest,
                                         HttpServletRequest request) {

        if (commentReportRequest.getReasons().contains("ETC") &&
                (commentReportRequest.getEtcReason() == null || commentReportRequest.getEtcReason().isBlank())) {
            throw new BadRequestException("이유를 적어야 합니다.", ErrorCode.MANDATORY_ETC_REASON);
        }

        if (!gameReportRepository.existsByGameIdAndComments(gameId, commentReportRequest)) {
            throw new NotFoundException("해당 댓글을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("잘못된 유저 정보입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (gameReportRepository.existsGameCommentReport(gameId, commentReportRequest, users.getUid())) {
            throw new BadRequestException("이미 신고한 댓글입니다.", ErrorCode.DUPLICATE_REPORT_EXCEPTION);
        }

        gameReportRepository.saveCommentReport(gameId, users, commentReportRequest);
    }

    @Transactional
    public void submitUserReport(UserReportRequest userReportRequest, HttpServletRequest request) {

        if (!userReportRequest.getTargetType().equals(ReportTargetType.USER)) {
            throw new BadRequestException("잘못된 요청입니다.", ErrorCode.INVALID_REPORT_TARGET_TYPE);
        }

        if (userReportRequest.getReasons().contains("ETC") &&
                (userReportRequest.getEtcReason() == null || userReportRequest.getEtcReason().isBlank())) {
            throw new BadRequestException("이유를 적어야 합니다.", ErrorCode.MANDATORY_ETC_REASON);
        }

        if (!userRepository.existsByNickname(userReportRequest.getNickname())) {
            throw new NotFoundException("해당 유저는 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("잘못된 유저 정보입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (gameReportRepository.existsUserReport(userReportRequest.getNickname(), users.getUid())) {
            throw new BadRequestException("이미 신고한 유저입니다.", ErrorCode.DUPLICATE_REPORT_EXCEPTION);
        }

        gameReportRepository.saveUserReport(users, userReportRequest);
    }
}
