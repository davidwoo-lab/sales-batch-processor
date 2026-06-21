package com.davidlab.salesbatch.batch.trigger;

import com.davidlab.salesbatch.batch.trigger.dto.JobExecutionResult;
import com.davidlab.salesbatch.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * 배치 Job 수동 실행 API.
 * 스케줄과 별개로, 특정 날짜/파일 기준으로 배치를 즉시 실행한다.
 */
@Tag(name = "배치 실행", description = "매출 배치 Job 수동 실행 API")
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job csvImportJob;
    private final Job salesAggregationJob;

    public BatchController(JobLauncher jobLauncher,
                           @Qualifier("csvImportJob") Job csvImportJob,
                           @Qualifier("salesAggregationJob") Job salesAggregationJob) {
        this.jobLauncher = jobLauncher;
        this.csvImportJob = csvImportJob;
        this.salesAggregationJob = salesAggregationJob;
    }

    @Operation(summary = "CSV 적재 배치 수동 실행",
            description = "지정한 CSV 파일을 읽어 매출 원본 테이블에 적재합니다. "
                    + "targetDate를 지정하면 해당 일자 기존 데이터를 삭제한 뒤 적재해 재적재 시 중복을 방지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 실행 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배치 실행 중 오류 발생")
    })
    @PostMapping("/csv-import")
    public ResponseEntity<ApiResponse<JobExecutionResult>> runCsvImport(
            @Parameter(description = "적재할 CSV 파일 경로", example = "/data/sales/import/2026-06-20.csv")
            @RequestParam String filePath,
            @Parameter(description = "재적재 멱등성 기준 일자 (yyyy-MM-dd, 생략 시 신규 적재)", example = "2026-06-20")
            @RequestParam(required = false) String targetDate) throws Exception {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("filePath", filePath)
                .addLong("timestamp", System.currentTimeMillis());
        if (targetDate != null) {
            builder.addString("targetDate", parseDate(targetDate).toString());
        }
        JobExecution execution = jobLauncher.run(csvImportJob, builder.toJobParameters());
        return ResponseEntity.ok(ApiResponse.success(JobExecutionResult.from(execution)));
    }

    @Operation(summary = "매출 집계 배치 수동 실행",
            description = "지정한 날짜 기준으로 일/월 매출 집계 배치를 수동으로 실행합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 실행 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배치 실행 중 오류 발생")
    })
    @PostMapping("/aggregation")
    public ResponseEntity<ApiResponse<JobExecutionResult>> runAggregation(
            @Parameter(description = "집계 대상 날짜 (yyyy-MM-dd)", example = "2026-06-20")
            @RequestParam String targetDate) throws Exception {
        LocalDate parsed = parseDate(targetDate);
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", parsed.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(salesAggregationJob, params);
        return ResponseEntity.ok(ApiResponse.success(JobExecutionResult.from(execution)));
    }

    /**
     * 날짜 문자열을 파싱한다. 형식 오류 시 IllegalArgumentException으로 변환해 400 응답을 유도한다.
     */
    private LocalDate parseDate(String targetDate) {
        try {
            return LocalDate.parse(targetDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. (yyyy-MM-dd): " + targetDate);
        }
    }
}
