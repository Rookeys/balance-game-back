package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.report.GameCommentReportRequest;
import com.games.balancegameback.dto.game.report.GameReportRequest;
import com.games.balancegameback.dto.user.UserReportRequest;

public interface GameReportRepository {

    boolean existsByGameId(Long gameId);

    boolean existsByGameIdAndResourceId(Long gameId, Long resourceId);

    boolean existsByGameIdAndComments(Long gameId, GameCommentReportRequest commentReportRequest);

    void saveGameReport(Long gameId, Users users, GameReportRequest gameReportRequest);

    void saveGameResourceReport(Long gameId, Long resourceId, Users users, GameReportRequest gameReportRequest);

    void saveCommentReport(Long gameId, Users users, GameCommentReportRequest commentReportRequest);

    void saveUserReport(Users users, UserReportRequest userReportRequest);

    boolean existsGameReport(Long gameId, String uid);

    boolean existsGameResourceReport(Long gameId, Long resourceId, String uid);

    boolean existsGameCommentReport(Long gameId, GameCommentReportRequest commentReportRequest, String uid);

    boolean existsUserReport(String nickname, String uid);
}
