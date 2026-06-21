package com.davidlab.salesbatch.sales.dto;

import com.davidlab.salesbatch.sales.domain.SalesSummaryMonthly;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 월별 매출 집계 조회 응답 DTO.
 */
@Schema(description = "월별 매출 집계 결과")
public record MonthlySalesResponse(
        @Schema(description = "집계 월(yyyy-MM)", example = "2026-06")
        String salesMonth,

        @Schema(description = "매장 식별자", example = "STORE001")
        String storeId,

        @Schema(description = "총 판매 수량", example = "152")
        long totalQuantity,

        @Schema(description = "총 매출 금액(원)", example = "689000")
        long totalAmount
) {
    public static MonthlySalesResponse from(SalesSummaryMonthly entity) {
        return new MonthlySalesResponse(
                entity.getSalesMonth(),
                entity.getStoreId(),
                entity.getTotalQuantity(),
                entity.getTotalAmount());
    }
}
