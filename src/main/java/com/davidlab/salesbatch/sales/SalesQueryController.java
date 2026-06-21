package com.davidlab.salesbatch.sales;

import com.davidlab.salesbatch.common.response.ApiResponse;
import com.davidlab.salesbatch.sales.dto.DailySalesResponse;
import com.davidlab.salesbatch.sales.dto.MonthlySalesResponse;
import com.davidlab.salesbatch.sales.repository.SalesSummaryDailyRepository;
import com.davidlab.salesbatch.sales.repository.SalesSummaryMonthlyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 매출 집계 결과 조회 API.
 */
@Tag(name = "매출 조회", description = "일/월 매출 집계 결과 조회 API")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesQueryController {

    private final SalesSummaryDailyRepository dailyRepository;
    private final SalesSummaryMonthlyRepository monthlyRepository;

    @Operation(summary = "일별 매출 집계 조회", description = "지정한 일자의 매장별 일 매출 집계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
    })
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailySalesResponse>>> getDaily(
            @Parameter(description = "조회 일자 (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam String date) {
        LocalDate salesDate = parseDate(date);
        List<DailySalesResponse> result = dailyRepository.findBySalesDate(salesDate).stream()
                .map(DailySalesResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "월별 매출 집계 조회", description = "지정한 월(yyyy-MM)의 매장별 월 매출 집계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 월 형식")
    })
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlySalesResponse>>> getMonthly(
            @Parameter(description = "조회 월 (yyyy-MM)", example = "2026-06")
            @RequestParam String month) {
        String salesMonth = parseMonth(month);
        List<MonthlySalesResponse> result = monthlyRepository.findBySalesMonth(salesMonth).stream()
                .map(MonthlySalesResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 일자 문자열을 파싱한다. 형식 오류 시 400 응답을 유도한다.
     */
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. (yyyy-MM-dd): " + date);
        }
    }

    /**
     * 월 문자열을 파싱한다. 형식 검증 후 yyyy-MM 문자열로 반환한다.
     */
    private String parseMonth(String month) {
        try {
            return YearMonth.parse(month).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 월 형식입니다. (yyyy-MM): " + month);
        }
    }
}
