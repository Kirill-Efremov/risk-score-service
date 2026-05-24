package ru.kpfu.itis.efremov.schemarisk.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Решение администратора по заявке на согласование публикации схемы")
public class ApprovalDecisionRequest {

    @Schema(description = "Комментарий администратора к решению")
    private String comment;
}
