package com.davidlab.salesbatch.batch.aggregation;

import com.davidlab.salesbatch.notification.EmailReportSender;
import com.davidlab.salesbatch.sales.domain.SalesRawData;
import com.davidlab.salesbatch.sales.domain.SalesSummaryDaily;
import com.davidlab.salesbatch.sales.domain.SalesSummaryMonthly;
import com.davidlab.salesbatch.sales.repository.DashboardSummaryRepository;
import com.davidlab.salesbatch.sales.repository.SalesRawDataRepository;
import com.davidlab.salesbatch.sales.repository.SalesSummaryDailyRepository;
import com.davidlab.salesbatch.sales.repository.SalesSummaryMonthlyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * salesAggregationJob 통합 테스트.
 * 샘플 데이터 기준 일/월 집계 결과값과 대시보드 저장, 성공 리포트 호출을 검증한다.
 * 실제 메일 발송을 막기 위해 EmailReportSender는 Mock으로 대체한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class SalesAggregationJobTest {

    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 6, 1);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("salesAggregationJob")
    private Job salesAggregationJob;

    @Autowired
    private SalesRawDataRepository rawDataRepository;

    @Autowired
    private SalesSummaryDailyRepository dailyRepository;

    @Autowired
    private SalesSummaryMonthlyRepository monthlyRepository;

    @Autowired
    private DashboardSummaryRepository dashboardSummaryRepository;

    @MockBean
    private EmailReportSender emailReportSender;

    @AfterEach
    void tearDown() {
        dashboardSummaryRepository.deleteAll();
        dailyRepository.deleteAll();
        monthlyRepository.deleteAll();
        rawDataRepository.deleteAll();
    }

    @Test
    @DisplayName("샘플 데이터를 일/월 집계하고 대시보드 저장 및 성공 리포트를 호출한다")
    void aggregation_sampleData_aggregatesAndNotifies() throws Exception {
        // given: STORE001 합계 qty=5/amount=23500, STORE002 qty=5/amount=22500
        rawDataRepository.save(raw("STORE001", 3, 4500, 13500));
        rawDataRepository.save(raw("STORE001", 2, 5000, 10000));
        rawDataRepository.save(raw("STORE002", 5, 4500, 22500));

        // when
        JobExecution execution = jobLauncher.run(salesAggregationJob, params());

        // then: Job 성공
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 일별 집계 검증
        SalesSummaryDaily store1 = dailyRepository.findBySalesDateAndStoreId(TARGET_DATE, "STORE001").orElseThrow();
        assertThat(store1.getTotalQuantity()).isEqualTo(5);
        assertThat(store1.getTotalAmount()).isEqualTo(23500);
        assertThat(dailyRepository.findBySalesDate(TARGET_DATE)).hasSize(2);

        // 월별 집계 검증
        SalesSummaryMonthly monthly1 = monthlyRepository.findBySalesMonthAndStoreId("2026-06", "STORE001").orElseThrow();
        assertThat(monthly1.getTotalAmount()).isEqualTo(23500);

        // 대시보드 스냅샷 저장 + 성공 리포트 호출 검증
        assertThat(dashboardSummaryRepository.count()).isEqualTo(1);
        verify(emailReportSender).sendSuccessReport(any());
    }

    private SalesRawData raw(String storeId, int quantity, long unitPrice, long totalAmount) {
        return SalesRawData.builder()
                .orderDate(TARGET_DATE)
                .storeId(storeId)
                .productName("PRODUCT")
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .build();
    }

    private JobParameters params() {
        return new JobParametersBuilder()
                .addString("targetDate", TARGET_DATE.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
