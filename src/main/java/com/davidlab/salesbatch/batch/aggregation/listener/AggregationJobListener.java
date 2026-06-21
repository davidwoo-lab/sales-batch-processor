package com.davidlab.salesbatch.batch.aggregation.listener;

import com.davidlab.salesbatch.dashboard.DashboardSummaryService;
import com.davidlab.salesbatch.notification.EmailReportSender;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 집계 Job 완료 후 대시보드 저장 및 결과 이메일 발송을 트리거하는 리스너.
 * Job 성공 시 대시보드 스냅샷 저장 + 성공 리포트, 실패 시 실패 알림을 발송한다.
 */
@Component
@RequiredArgsConstructor
public class AggregationJobListener implements JobExecutionListener {

    private final EmailReportSender emailReportSender;
    private final DashboardSummaryService dashboardSummaryService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            dashboardSummaryService.saveSummary(jobExecution);
            emailReportSender.sendSuccessReport(jobExecution);
        } else {
            emailReportSender.sendFailureAlert(jobExecution);
        }
    }
}
