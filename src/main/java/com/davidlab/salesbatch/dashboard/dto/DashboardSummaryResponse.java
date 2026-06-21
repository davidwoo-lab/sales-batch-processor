package com.davidlab.salesbatch.dashboard.dto;

import com.davidlab.salesbatch.sales.domain.DashboardSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 대시보드 스냅샷 조회 응답 DTO.
 */
@Schema(description = "대시보드 집계 스냅샷")
public record DashboardSummaryResponse(
        @Schema(description = "스냅샷 ID", example = "1")
        Long id,

        @Schema(description = "스냅샷을 생성한 배치 실행 ID", example = "12")
        Long jobExecutionId,

        @Schema(description = "집계 기준 일자", example = "2026-06-01")
        LocalDate summaryDate,

        @Schema(description = "총 매출 금액(원)", example = "71500")
        long totalAmount,

        @Schema(description = "처리 건수", example = "5")
        long processedCount
) {
    public static DashboardSummaryResponse from(DashboardSummary entity) {
        return new DashboardSummaryResponse(
                entity.getId(),
                entity.getJobExecutionId(),
                entity.getSummaryDate(),
                entity.getTotalAmount(),
                entity.getProcessedCount());
    }
}
