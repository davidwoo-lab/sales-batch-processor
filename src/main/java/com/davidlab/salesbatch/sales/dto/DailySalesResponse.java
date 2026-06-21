package com.davidlab.salesbatch.sales.dto;

import com.davidlab.salesbatch.sales.domain.SalesSummaryDaily;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 일별 매출 집계 조회 응답 DTO.
 */
@Schema(description = "일별 매출 집계 결과")
public record DailySalesResponse(
        @Schema(description = "집계 일자", example = "2026-06-01")
        LocalDate salesDate,

        @Schema(description = "매장 식별자", example = "STORE001")
        String storeId,

        @Schema(description = "총 판매 수량", example = "5")
        long totalQuantity,

        @Schema(description = "총 매출 금액(원)", example = "23500")
        long totalAmount
) {
    public static DailySalesResponse from(SalesSummaryDaily entity) {
        return new DailySalesResponse(
                entity.getSalesDate(),
                entity.getStoreId(),
                entity.getTotalQuantity(),
                entity.getTotalAmount());
    }
}
