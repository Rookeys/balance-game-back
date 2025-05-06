package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import com.games.balancegameback.domain.user.Users;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class Report {

    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private String targetUid;
    private Users reporter;
    private List<String> reasons;
    private String etcReason;

    @Builder
    public Report(Long id, ReportTargetType targetType, Long targetId, String targetUid, Users reporter,
                  List<String> reasons, String etcReason) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetUid = targetUid;
        this.reporter = reporter;
        this.reasons = reasons;
        this.etcReason = etcReason;
    }
}
