package com.davidlab.salesbatch.sales.repository;

import com.davidlab.salesbatch.sales.domain.SalesSummaryDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일별 매출 집계 Repository.
 */
public interface SalesSummaryDailyRepository extends JpaRepository<SalesSummaryDaily, Long> {

    /** 재집계 시 기존 행을 찾기 위한 조회 */
    Optional<SalesSummaryDaily> findBySalesDateAndStoreId(LocalDate salesDate, String storeId);

    /** 특정 일자의 모든 매장 집계 조회 (대시보드 스냅샷 산출용) */
    List<SalesSummaryDaily> findBySalesDate(LocalDate salesDate);
}
