package com.davidlab.salesbatch.sales.repository.projection;

/**
 * 매출 집계 쿼리(GROUP BY) 결과를 담는 Projection.
 * 매장 단위로 합산된 수량/금액을 표현한다.
 */
public interface SalesAggregationResult {

    /** 매장 식별자 */
    String getStoreId();

    /** 합산 수량 */
    long getTotalQuantity();

    /** 합산 금액 (원) */
    long getTotalAmount();
}
