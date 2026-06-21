package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.repository.SalesRawDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * csvImportJob 통합 테스트.
 * 정상 적재와 파싱 오류 라인 skip 동작을 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class CsvImportJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("csvImportJob")
    private Job csvImportJob;

    @Autowired
    private SalesRawDataRepository rawDataRepository;

    @TempDir
    private Path tempDir;

    @AfterEach
    void tearDown() {
        rawDataRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 CSV는 모든 행이 적재된다")
    void csvImport_validFile_savesAll() throws Exception {
        Path csv = tempDir.resolve("sales.csv");
        Files.writeString(csv, """
                order_date,store_id,product_name,quantity,unit_price,total_amount
                2026-06-01,STORE001,COFFEE,3,4500,13500
                2026-06-01,STORE002,LATTE,2,5000,10000
                """);

        JobExecution execution = jobLauncher.run(csvImportJob, paramsForFile(csv));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(rawDataRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("파싱 오류가 있는 라인은 skip되고 나머지는 적재된다")
    void csvImport_invalidLine_skipsAndContinues() throws Exception {
        Path csv = tempDir.resolve("sales_invalid.csv");
        // 둘째 행의 quantity가 숫자가 아니므로 파싱 단계에서 skip 대상이 된다
        Files.writeString(csv, """
                order_date,store_id,product_name,quantity,unit_price,total_amount
                2026-06-01,STORE001,COFFEE,abc,4500,13500
                2026-06-01,STORE002,LATTE,2,5000,10000
                """);

        JobExecution execution = jobLauncher.run(csvImportJob, paramsForFile(csv));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(rawDataRepository.count()).isEqualTo(1);

        StepExecution importStep = execution.getStepExecutions().stream()
                .filter(step -> step.getStepName().equals("csvImportStep"))
                .findFirst()
                .orElseThrow();
        assertThat(importStep.getReadSkipCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("targetDate를 주고 동일 파일을 재적재해도 데이터가 중복되지 않는다")
    void csvImport_reRunWithTargetDate_isIdempotent() throws Exception {
        Path csv = tempDir.resolve("sales_idempotent.csv");
        Files.writeString(csv, """
                order_date,store_id,product_name,quantity,unit_price,total_amount
                2026-06-01,STORE001,COFFEE,3,4500,13500
                2026-06-01,STORE002,LATTE,2,5000,10000
                """);

        // 1차 적재
        jobLauncher.run(csvImportJob, paramsForFileAndDate(csv, "2026-06-01"));
        assertThat(rawDataRepository.count()).isEqualTo(2);

        // 동일 파일 재적재 → purge 후 적재되어 여전히 2건 (중복 없음)
        jobLauncher.run(csvImportJob, paramsForFileAndDate(csv, "2026-06-01"));
        assertThat(rawDataRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("적재 파일이 없으면 기존 데이터를 삭제하지 않고 Job이 실패한다 (데이터 보존)")
    void csvImport_missingFile_preservesExistingData() throws Exception {
        // given: 기존 6/1 데이터가 적재되어 있음
        Path existing = tempDir.resolve("existing.csv");
        Files.writeString(existing, """
                order_date,store_id,product_name,quantity,unit_price,total_amount
                2026-06-01,STORE001,COFFEE,3,4500,13500
                """);
        jobLauncher.run(csvImportJob, paramsForFileAndDate(existing, "2026-06-01"));
        assertThat(rawDataRepository.count()).isEqualTo(1);

        // when: 존재하지 않는 파일을 동일 targetDate로 적재 시도
        Path missing = tempDir.resolve("does-not-exist.csv");
        JobExecution execution = jobLauncher.run(csvImportJob, paramsForFileAndDate(missing, "2026-06-01"));

        // then: Job은 실패하지만 기존 데이터는 삭제되지 않고 보존됨
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(rawDataRepository.count()).isEqualTo(1);
    }

    private JobParameters paramsForFile(Path csv) {
        return new JobParametersBuilder()
                .addString("filePath", csv.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    private JobParameters paramsForFileAndDate(Path csv, String targetDate) {
        return new JobParametersBuilder()
                .addString("filePath", csv.toString())
                .addString("targetDate", targetDate)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
