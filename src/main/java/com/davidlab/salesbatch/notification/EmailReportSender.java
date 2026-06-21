package com.davidlab.salesbatch.notification;

import com.davidlab.salesbatch.batch.aggregation.AggregationContextKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 집계 결과 리포트를 이메일로 발송한다.
 * 발송 실패는 배치 Job 실패로 처리하지 않고 로그만 남긴다.
 */
@Slf4j
@Component
public class EmailReportSender {

    private final JavaMailSender mailSender;
    private final String recipient;

    public EmailReportSender(JavaMailSender mailSender,
                             @Value("${report.recipient}") String recipient) {
        this.mailSender = mailSender;
        this.recipient = recipient;
    }

    /**
     * 집계 성공 시 요약 리포트를 발송한다.
     */
    public void sendSuccessReport(JobExecution jobExecution) {
        ExecutionContext context = jobExecution.getExecutionContext();
        String targetDate = context.getString(AggregationContextKeys.TARGET_DATE, "-");
        long totalAmount = context.getLong(AggregationContextKeys.TOTAL_AMOUNT, 0L);
        long processedCount = context.getLong(AggregationContextKeys.PROCESSED_COUNT, 0L);
        int storeCount = context.getInt(AggregationContextKeys.STORE_COUNT, 0);

        String subject = String.format("[매출 배치] %s 집계 완료", targetDate);
        String body = """
                안녕하세요, %s 매출 집계 결과를 안내드립니다.

                - 처리 건수: %,d건
                - 총 매출: %,d원
                - 처리 매장 수: %,d개

                상세 내역은 대시보드에서 확인 가능합니다.
                """.formatted(targetDate, processedCount, totalAmount, storeCount);

        send(subject, body);
    }

    /**
     * 집계 실패 시 에러 알림을 발송한다.
     */
    public void sendFailureAlert(JobExecution jobExecution) {
        String subject = "[매출 배치] 집계 실패 알림";
        String body = """
                매출 집계 배치가 실패했습니다.

                - Job 실행 ID: %d
                - 상태: %s
                - 종료 코드: %s

                로그를 확인해 주세요.
                """.formatted(
                jobExecution.getId(),
                jobExecution.getStatus(),
                jobExecution.getExitStatus().getExitCode());

        send(subject, body);
    }

    /**
     * 실제 메일 발송. 실패해도 예외를 전파하지 않고 로그만 남긴다.
     */
    private void send(String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("결과 이메일 발송 완료: to={}, subject={}", recipient, subject);
        } catch (Exception e) {
            // 이메일 발송 실패는 배치 실패로 간주하지 않음
            log.error("결과 이메일 발송 실패: subject={}", subject, e);
        }
    }
}
