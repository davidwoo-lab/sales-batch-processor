package com.davidlab.salesbatch.batch.aggregation;

import com.davidlab.salesbatch.batch.aggregation.listener.AggregationJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job 2 — salesAggregationJob 설정.
 * 집계는 대량 GROUP BY 쿼리로 처리하는 것이 효율적이므로 Tasklet 방식을 사용한다.
 * 흐름: dailyAggregationStep → monthlyAggregationStep (통보는 JobExecutionListener afterJob에서 처리)
 */
@Configuration
@RequiredArgsConstructor
public class SalesAggregationJobConfig {

    /**
     * 일별 집계 Step.
     */
    @Bean
    public Step dailyAggregationStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     DailySalesAggregationTasklet tasklet) {
        return new StepBuilder("dailyAggregationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    /**
     * 월별 집계 Step.
     */
    @Bean
    public Step monthlyAggregationStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       MonthlySalesAggregationTasklet tasklet) {
        return new StepBuilder("monthlyAggregationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    /**
     * 매출 집계 Job. 일별 → 월별 순서로 실행하고,
     * 완료 후 리스너(afterJob)에서 대시보드 저장 및 결과 이메일을 발송한다.
     */
    @Bean
    public Job salesAggregationJob(JobRepository jobRepository,
                                   Step dailyAggregationStep,
                                   Step monthlyAggregationStep,
                                   AggregationJobListener aggregationJobListener) {
        return new JobBuilder("salesAggregationJob", jobRepository)
                .listener(aggregationJobListener)
                .start(dailyAggregationStep)
                .next(monthlyAggregationStep)
                .build();
    }
}
