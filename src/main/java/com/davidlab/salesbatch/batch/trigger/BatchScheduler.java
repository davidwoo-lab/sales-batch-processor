package com.davidlab.salesbatch.batch.trigger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 배치 Job을 정해진 스케줄에 따라 실행하는 트리거.
 * cron 표현식은 application.yml의 scheduler.* 설정에서 주입받는다.
 */
@Slf4j
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job csvImportJob;
    private final Job salesAggregationJob;
    private final String csvImportPath;

    public BatchScheduler(JobLauncher jobLauncher,
                          @Qualifier("csvImportJob") Job csvImportJob,
                          @Qualifier("salesAggregationJob") Job salesAggregationJob,
                          @Value("${scheduler.csv-import-path}") String csvImportPath) {
        this.jobLauncher = jobLauncher;
        this.csvImportJob = csvImportJob;
        this.salesAggregationJob = salesAggregationJob;
        this.csvImportPath = csvImportPath;
    }

    /**
     * 매일 새벽 1시: 전날 매출 CSV 파일을 적재한다.
     * 파일 경로 규칙: {csv-import-path}/{yyyy-MM-dd}.csv
     */
    @Scheduled(cron = "${scheduler.csv-import-cron}")
    public void runCsvImport() {
        LocalDate target = LocalDate.now().minusDays(1);
        String filePath = "%s/%s.csv".formatted(csvImportPath, target);
        JobParameters params = new JobParametersBuilder()
                .addString("filePath", filePath)
                .addString("targetDate", target.toString()) // 재적재 멱등성: 해당 일자 기존 데이터 삭제 후 적재
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        runJob(csvImportJob, params, "csvImportJob");
    }

    /**
     * 매일 새벽 2시: 전날 매출을 일/월 단위로 집계한다.
     */
    @Scheduled(cron = "${scheduler.aggregation-cron}")
    public void runAggregation() {
        LocalDate target = LocalDate.now().minusDays(1);
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", target.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        runJob(salesAggregationJob, params, "salesAggregationJob");
    }

    /**
     * Job을 실행하고 예외 발생 시 로그를 남긴다. (스케줄 실행 실패가 다른 스케줄에 영향 주지 않도록)
     */
    private void runJob(Job job, JobParameters params, String jobName) {
        try {
            log.info("스케줄 배치 실행 시작: {}", jobName);
            jobLauncher.run(job, params);
        } catch (Exception e) {
            log.error("스케줄 배치 실행 실패: {}", jobName, e);
        }
    }
}
