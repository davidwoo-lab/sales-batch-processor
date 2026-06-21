package com.davidlab.salesbatch.sales.repository;

import com.davidlab.salesbatch.sales.domain.SalesSummaryMonthly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 월별 매출 집계 Repository.
 */
public interface SalesSummaryMonthlyRepository extends JpaRepository<SalesSummaryMonthly, Long> {

    /** 재집계 시 기존 행을 찾기 위한 조회 */
    Optional<SalesSummaryMonthly> findBySalesMonthAndStoreId(String salesMonth, String storeId);

    /** 특정 월의 모든 매장 집계 조회 */
    List<SalesSummaryMonthly> findBySalesMonth(String salesMonth);
}
