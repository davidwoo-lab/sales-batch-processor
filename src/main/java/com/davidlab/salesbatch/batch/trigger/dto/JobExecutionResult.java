package com.davidlab.salesbatch.batch.trigger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.batch.core.JobExecution;

/**
 * 배치 Job 실행 결과 응답 DTO.
 *
 * @param jobName     실행한 Job 이름
 * @param executionId Job 실행 ID
 * @param status      배치 상태 (COMPLETED, FAILED 등)
 * @param exitCode    종료 코드
 */
@Schema(description = "배치 Job 실행 결과")
public record JobExecutionResult(
        @Schema(description = "실행한 Job 이름", example = "salesAggregationJob")
        String jobName,

        @Schema(description = "Job 실행 ID", example = "12")
        Long executionId,

        @Schema(description = "배치 상태", example = "COMPLETED")
        String status,

        @Schema(description = "종료 코드", example = "COMPLETED")
        String exitCode
) {

    /**
     * JobExecution으로부터 응답 DTO를 생성한다.
     */
    public static JobExecutionResult from(JobExecution jobExecution) {
        return new JobExecutionResult(
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getId(),
                jobExecution.getStatus().toString(),
                jobExecution.getExitStatus().getExitCode());
    }
}
