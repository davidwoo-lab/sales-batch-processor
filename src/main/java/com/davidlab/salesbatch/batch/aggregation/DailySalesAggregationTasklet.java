package com.davidlab.salesbatch.batch.aggregation;

import com.davidlab.salesbatch.sales.domain.SalesSummaryDaily;
import com.davidlab.salesbatch.sales.repository.SalesRawDataRepository;
import com.davidlab.salesbatch.sales.repository.SalesSummaryDailyRepository;
import com.davidlab.salesbatch.sales.repository.projection.SalesAggregationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 매출 집계 Tasklet.
 * 대상 일자의 원본 데이터를 매장 단위로 GROUP BY 집계해 일별 집계 테이블에 upsert한다.
 * 집계 통계(총액/건수/매장수)는 JobExecutionContext에 저장해 통보 리스너에서 활용한다.
 */
@Slf4j
@Component
@StepScope
public class DailySalesAggregationTasklet implements Tasklet {

    private final SalesRawDataRepository rawDataRepository;
    private final SalesSummaryDailyRepository dailyRepository;
    private final LocalDate targetDate;

    public DailySalesAggregationTasklet(SalesRawDataRepository rawDataRepository,
                                        SalesSummaryDailyRepository dailyRepository,
                                        @Value("#{jobParameters['targetDate']}") String targetDate) {
        this.rawDataRepository = rawDataRepository;
        this.dailyRepository = dailyRepository;
        this.targetDate = LocalDate.parse(targetDate);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<SalesAggregationResult> results = rawDataRepository.aggregateDailyByStore(targetDate);
        log.info("일별 집계 시작: targetDate={}, 매장 수={}", targetDate, results.size());

        long totalAmount = 0L;
        for (SalesAggregationResult result : results) {
            upsertDaily(result);
            totalAmount += result.getTotalAmount();
        }

        long processedCount = rawDataRepository.countByOrderDate(targetDate);
        saveToJobContext(chunkContext, totalAmount, processedCount, results.size());

        log.info("일별 집계 완료: targetDate={}, 총매출={}, 처리건수={}, 매장수={}",
                targetDate, totalAmount, processedCount, results.size());
        return RepeatStatus.FINISHED;
    }

    /**
     * 매장별 집계 결과를 upsert한다. 기존 행이 있으면 갱신, 없으면 신규 저장한다.
     */
    private void upsertDaily(SalesAggregationResult result) {
        dailyRepository.findBySalesDateAndStoreId(targetDate, result.getStoreId())
                .ifPresentOrElse(
                        existing -> existing.updateAmounts(result.getTotalQuantity(), result.getTotalAmount()),
                        () -> dailyRepository.save(SalesSummaryDaily.builder()
                                .salesDate(targetDate)
                                .storeId(result.getStoreId())
                                .totalQuantity(result.getTotalQuantity())
                                .totalAmount(result.getTotalAmount())
                                .build())
                );
    }

    /**
     * 집계 통계를 JobExecutionContext에 저장해 통보 리스너로 전달한다.
     */
    private void saveToJobContext(ChunkContext chunkContext, long totalAmount, long processedCount, int storeCount) {
        ExecutionContext jobContext = chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext();
        jobContext.putString(AggregationContextKeys.TARGET_DATE, targetDate.toString());
        jobContext.putLong(AggregationContextKeys.TOTAL_AMOUNT, totalAmount);
        jobContext.putLong(AggregationContextKeys.PROCESSED_COUNT, processedCount);
        jobContext.putInt(AggregationContextKeys.STORE_COUNT, storeCount);
    }
}
