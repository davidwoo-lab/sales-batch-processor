package com.davidlab.salesbatch.sales.domain;

import com.davidlab.salesbatch.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 대시보드 조회용 집계 스냅샷.
 * 원본 집계 테이블과 분리해 대시보드 조회 성능에 영향을 주지 않도록 한다.
 */
@Getter
@Entity
@Table(name = "dashboard_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 스냅샷을 생성한 배치 실행 ID */
    @Column(name = "job_execution_id", nullable = false)
    private Long jobExecutionId;

    /** 집계 기준 일자 */
    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    /** 총 매출 금액 (원) */
    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    /** 처리 건수 */
    @Column(name = "processed_count", nullable = false)
    private long processedCount;

    @Builder
    private DashboardSummary(Long jobExecutionId, LocalDate summaryDate, long totalAmount, long processedCount) {
        this.jobExecutionId = jobExecutionId;
        this.summaryDate = summaryDate;
        this.totalAmount = totalAmount;
        this.processedCount = processedCount;
    }
}
