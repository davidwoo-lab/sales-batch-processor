package com.davidlab.salesbatch.dashboard;

import com.davidlab.salesbatch.common.response.ApiResponse;
import com.davidlab.salesbatch.dashboard.dto.DashboardSummaryResponse;
import com.davidlab.salesbatch.sales.repository.DashboardSummaryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 대시보드 스냅샷 조회 API.
 */
@Tag(name = "대시보드", description = "대시보드 집계 스냅샷 조회 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardSummaryRepository dashboardSummaryRepository;

    @Operation(summary = "대시보드 스냅샷 목록 조회", description = "집계 완료 시점에 저장된 대시보드 스냅샷을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DashboardSummaryResponse>>> getSummaries() {
        List<DashboardSummaryResponse> result = dashboardSummaryRepository.findAllByOrderBySummaryDateDescIdDesc()
                .stream()
                .map(DashboardSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
