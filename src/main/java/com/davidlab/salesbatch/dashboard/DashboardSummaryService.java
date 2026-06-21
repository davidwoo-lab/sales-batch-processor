package com.davidlab.salesbatch.dashboard;

import com.davidlab.salesbatch.batch.aggregation.AggregationContextKeys;
import com.davidlab.salesbatch.sales.domain.DashboardSummary;
import com.davidlab.salesbatch.sales.repository.DashboardSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 집계 완료 시점의 결과를 대시보드용 스냅샷 테이블에 저장한다.
 * 원본 집계 테이블과 분리해 대시보드 조회 성능에 영향을 주지 않도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardSummaryService {

    private final DashboardSummaryRepository dashboardSummaryRepository;

    /**
     * JobExecutionContext의 집계 통계를 읽어 대시보드 스냅샷을 저장한다.
     */
    @Transactional
    public void saveSummary(JobExecution jobExecution) {
        ExecutionContext context = jobExecution.getExecutionContext();
        String targetDate = context.getString(AggregationContextKeys.TARGET_DATE, null);
        if (targetDate == null) {
            log.warn("집계 통계가 없어 대시보드 스냅샷 저장을 건너뜁니다. jobExecutionId={}", jobExecution.getId());
            return;
        }

        long totalAmount = context.getLong(AggregationContextKeys.TOTAL_AMOUNT, 0L);
        long processedCount = context.getLong(AggregationContextKeys.PROCESSED_COUNT, 0L);

        DashboardSummary summary = DashboardSummary.builder()
                .jobExecutionId(jobExecution.getId())
                .summaryDate(LocalDate.parse(targetDate))
                .totalAmount(totalAmount)
                .processedCount(processedCount)
                .build();
        dashboardSummaryRepository.save(summary);

        log.info("대시보드 스냅샷 저장 완료: summaryDate={}, totalAmount={}, processedCount={}",
                targetDate, totalAmount, processedCount);
    }
}
