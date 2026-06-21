package com.davidlab.salesbatch.sales;

import com.davidlab.salesbatch.sales.domain.SalesSummaryDaily;
import com.davidlab.salesbatch.sales.repository.SalesSummaryDailyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 매출 조회 API 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalesQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SalesSummaryDailyRepository dailyRepository;

    @AfterEach
    void tearDown() {
        dailyRepository.deleteAll();
    }

    @Test
    @DisplayName("일별 매출 집계를 조회한다")
    void getDaily_returnsSummaries() throws Exception {
        dailyRepository.save(SalesSummaryDaily.builder()
                .salesDate(LocalDate.of(2026, 6, 1))
                .storeId("STORE001")
                .totalQuantity(5)
                .totalAmount(23500)
                .build());

        mockMvc.perform(get("/api/sales/daily").param("date", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].storeId").value("STORE001"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(23500));
    }

    @Test
    @DisplayName("잘못된 날짜 형식은 400을 반환한다")
    void getDaily_invalidDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/sales/daily").param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
