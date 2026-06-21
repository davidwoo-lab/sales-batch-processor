package com.davidlab.salesbatch.sales.repository;

import com.davidlab.salesbatch.sales.domain.DashboardSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 대시보드 스냅샷 Repository.
 */
public interface DashboardSummaryRepository extends JpaRepository<DashboardSummary, Long> {

    /** 최신 스냅샷부터 조회 (대시보드 목록용) */
    List<DashboardSummary> findAllByOrderBySummaryDateDescIdDesc();
}
