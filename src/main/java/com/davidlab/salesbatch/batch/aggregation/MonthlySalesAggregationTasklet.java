package com.davidlab.salesbatch.batch.aggregation;

import com.davidlab.salesbatch.sales.domain.SalesSummaryMonthly;
import com.davidlab.salesbatch.sales.repository.SalesRawDataRepository;
import com.davidlab.salesbatch.sales.repository.SalesSummaryMonthlyRepository;
import com.davidlab.salesbatch.sales.repository.projection.SalesAggregationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 월별 매출 집계 Tasklet.
 * 대상 일자가 속한 월 전체(월초~월말)의 원본 데이터를 매장 단위로 집계해 월별 집계 테이블에 upsert한다.
 */
@Slf4j
@Component
@StepScope
public class MonthlySalesAggregationTasklet implements Tasklet {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SalesRawDataRepository rawDataRepository;
    private final SalesSummaryMonthlyRepository monthlyRepository;
    private final LocalDate targetDate;

    public MonthlySalesAggregationTasklet(SalesRawDataRepository rawDataRepository,
                                          SalesSummaryMonthlyRepository monthlyRepository,
                                          @Value("#{jobParameters['targetDate']}") String targetDate) {
        this.rawDataRepository = rawDataRepository;
        this.monthlyRepository = monthlyRepository;
        this.targetDate = LocalDate.parse(targetDate);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        YearMonth yearMonth = YearMonth.from(targetDate);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        String salesMonth = yearMonth.format(MONTH_FORMAT);

        List<SalesAggregationResult> results = rawDataRepository.aggregateMonthlyByStore(start, end);
        log.info("월별 집계 시작: salesMonth={}, 기간={}~{}, 매장 수={}", salesMonth, start, end, results.size());

        for (SalesAggregationResult result : results) {
            upsertMonthly(salesMonth, result);
        }

        log.info("월별 집계 완료: salesMonth={}, 매장수={}", salesMonth, results.size());
        return RepeatStatus.FINISHED;
    }

    /**
     * 매장별 월 집계 결과를 upsert한다. 기존 행이 있으면 갱신, 없으면 신규 저장한다.
     */
    private void upsertMonthly(String salesMonth, SalesAggregationResult result) {
        monthlyRepository.findBySalesMonthAndStoreId(salesMonth, result.getStoreId())
                .ifPresentOrElse(
                        existing -> existing.updateAmounts(result.getTotalQuantity(), result.getTotalAmount()),
                        () -> monthlyRepository.save(SalesSummaryMonthly.builder()
                                .salesMonth(salesMonth)
                                .storeId(result.getStoreId())
                                .totalQuantity(result.getTotalQuantity())
                                .totalAmount(result.getTotalAmount())
                                .build())
                );
    }
}
