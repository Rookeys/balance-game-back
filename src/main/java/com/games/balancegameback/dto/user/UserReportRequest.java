package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.game.enums.report.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserReportRequest {

    @Schema(description = "신고 유저 닉네임")
    @NotBlank(message = "신고할 유저의 닉네임은 필수입니다.")
    private String nickname;

    @Schema(description = "신고 유형")
    @NotNull(message = "신고 유형 선택은 필수입니다.")
    private ReportTargetType targetType;

    @Schema(description = "신고 사유 리스트")
    @Size(min = 1, message = "최소 1개의 신고 이유를 선택해야 합니다.")
    private List<String> reasons = new ArrayList<>();

    @Schema(description = "기타 상세 내용")
    private String etcReason;
}
