package com.davidlab.salesbatch.sales.repository;

import com.davidlab.salesbatch.sales.domain.SalesRawData;
import com.davidlab.salesbatch.sales.repository.projection.SalesAggregationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 매출 원본 데이터 Repository.
 * 집계 Job에서 사용하는 GROUP BY 집계 쿼리를 제공한다.
 */
public interface SalesRawDataRepository extends JpaRepository<SalesRawData, Long> {

    /**
     * 특정 일자의 매장별 매출을 집계한다.
     *
     * @param orderDate 집계 대상 일자
     * @return 매장별 집계 결과
     */
    @Query("""
            SELECT s.storeId AS storeId,
                   SUM(s.quantity) AS totalQuantity,
                   SUM(s.totalAmount) AS totalAmount
            FROM SalesRawData s
            WHERE s.orderDate = :orderDate
            GROUP BY s.storeId
            """)
    List<SalesAggregationResult> aggregateDailyByStore(@Param("orderDate") LocalDate orderDate);

    /**
     * 지정한 기간(월 범위)의 매장별 매출을 집계한다.
     *
     * @param startInclusive 시작일 (포함)
     * @param endInclusive   종료일 (포함)
     * @return 매장별 집계 결과
     */
    @Query("""
            SELECT s.storeId AS storeId,
                   SUM(s.quantity) AS totalQuantity,
                   SUM(s.totalAmount) AS totalAmount
            FROM SalesRawData s
            WHERE s.orderDate BETWEEN :startInclusive AND :endInclusive
            GROUP BY s.storeId
            """)
    List<SalesAggregationResult> aggregateMonthlyByStore(@Param("startInclusive") LocalDate startInclusive,
                                                         @Param("endInclusive") LocalDate endInclusive);

    /** 특정 일자의 원본 데이터 건수 (대시보드/이메일 처리 건수용) */
    long countByOrderDate(LocalDate orderDate);

    /**
     * 특정 일자의 원본 데이터를 일괄 삭제한다. (재적재 멱등성 확보용 벌크 삭제)
     *
     * @param orderDate 삭제 대상 일자
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM SalesRawData s WHERE s.orderDate = :orderDate")
    int deleteByOrderDate(@Param("orderDate") LocalDate orderDate);
}
