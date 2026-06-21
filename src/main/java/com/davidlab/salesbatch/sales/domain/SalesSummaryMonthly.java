package com.davidlab.salesbatch.sales.domain;

import com.davidlab.salesbatch.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 매출 집계 결과.
 * sales_month는 'yyyy-MM' 형식 문자열이며, (sales_month, store_id) 조합은 유일하다.
 */
@Getter
@Entity
@Table(name = "sales_summary_monthly", uniqueConstraints = {
        @UniqueConstraint(name = "uk_monthly_month_store", columnNames = {"sales_month", "store_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesSummaryMonthly extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 집계 대상 월 (yyyy-MM) */
    @Column(name = "sales_month", nullable = false, length = 7)
    private String salesMonth;

    /** 매장 식별자 */
    @Column(name = "store_id", nullable = false, length = 50)
    private String storeId;

    /** 총 판매 수량 */
    @Column(name = "total_quantity", nullable = false)
    private long totalQuantity;

    /** 총 매출 금액 (원) */
    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Builder
    private SalesSummaryMonthly(String salesMonth, String storeId, long totalQuantity, long totalAmount) {
        this.salesMonth = salesMonth;
        this.storeId = storeId;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
    }

    /**
     * 재집계 시 집계값을 갱신한다. (Setter 대신 도메인 메서드 사용)
     */
    public void updateAmounts(long totalQuantity, long totalAmount) {
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
    }
}
