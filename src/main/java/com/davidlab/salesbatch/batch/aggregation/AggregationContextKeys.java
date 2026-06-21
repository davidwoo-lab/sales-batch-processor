package com.davidlab.salesbatch.batch.aggregation;

/**
 * 집계 결과를 JobExecutionContext에 저장/조회할 때 사용하는 키 상수.
 * 일별 집계 Tasklet이 채우고, 통보 리스너(afterJob)가 읽어 이메일/대시보드에 활용한다.
 */
public final class AggregationContextKeys {

    private AggregationContextKeys() {
    }

    /** 집계 대상 일자 (yyyy-MM-dd 문자열) */
    public static final String TARGET_DATE = "aggregation.targetDate";

    /** 일별 총 매출 금액 (원) */
    public static final String TOTAL_AMOUNT = "aggregation.totalAmount";

    /** 처리 건수 (원본 데이터 행 수) */
    public static final String PROCESSED_COUNT = "aggregation.processedCount";

    /** 처리 매장 수 */
    public static final String STORE_COUNT = "aggregation.storeCount";
}
