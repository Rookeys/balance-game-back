package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.Report;
import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "reports")
public class ReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column
    private Long targetId;

    @Column
    private String targetUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uid", nullable = false, columnDefinition = "VARCHAR(255)")
    private UsersEntity reporter;

    // 신고 사유는 String 으로 통일해서 저장 (Enum.name())
    @ElementCollection
    @CollectionTable(name = "report_reasons", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "reason")
    private List<String> reasons;

    @Column
    private String etcReason;

    public static ReportEntity from(Report report) {
        ReportEntity entity = new ReportEntity();
        entity.id = report.getId();
        entity.targetType = report.getTargetType();
        entity.targetId = report.getTargetId();
        entity.targetUid = report.getTargetUid();
        entity.reporter = UsersEntity.from(report.getReporter());
        entity.reasons = report.getReasons();
        entity.etcReason = report.getEtcReason();

        return entity;
    }

    public Report toModel() {
        return Report.builder()
                .id(id)
                .targetType(targetType)
                .targetId(targetId)
                .targetUid(targetUid)
                .reporter(reporter.toModel())
                .reasons(reasons)
                .etcReason(etcReason)
                .build();
    }
}
