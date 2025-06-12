package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.Report;
import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_reporter_id", columnList = "reporter_id"),
        @Index(name = "idx_reports_target", columnList = "target_type, target_id")
})
public class ReportEntity extends BaseEntity {

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(name = "target_id", length = 50)
    private String targetId;

    @Column(name = "reporter_id", nullable = false, length = 36)
    private String reporterId;

    @ElementCollection
    @CollectionTable(name = "report_reasons", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "reason", length = 50)
    private List<String> reasons;

    @Column(length = 500)
    private String etcReason;

    @Override
    protected String getEntityPrefix() {
        return "RPT";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static ReportEntity from(Report report) {
        ReportEntity entity = new ReportEntity();
        entity.id = report.getId();
        entity.targetType = report.getTargetType();
        entity.targetId = report.getTargetId() != null ? String.valueOf(report.getTargetId()) : report.getTargetUid();
        entity.reporterId = report.getReporter().getUid();
        entity.reasons = report.getReasons();
        entity.etcReason = report.getEtcReason();
        return entity;
    }

    public Report toModel() {
        return Report.builder()
                .id(id)
                .targetType(targetType)
                .targetId(targetId != null && targetType != ReportTargetType.USER ? Long.valueOf(targetId) : null)
                .targetUid(targetType == ReportTargetType.USER ? targetId : null)
                .reasons(reasons)
                .etcReason(etcReason)
                .build();
    }
}
