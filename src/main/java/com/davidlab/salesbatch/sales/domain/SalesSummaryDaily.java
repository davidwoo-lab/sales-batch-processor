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

import java.time.LocalDate;

/**
 * 일별 매출 집계 결과.
 * (sales_date, store_id) 조합은 유일하며, 재집계 시 기존 행을 갱신한다.
 */
@Getter
@Entity
@Table(name = "sales_summary_daily", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_date_store", columnNames = {"sales_date", "store_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesSummaryDaily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 집계 대상 일자 */
    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

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
    private SalesSummaryDaily(LocalDate salesDate, String storeId, long totalQuantity, long totalAmount) {
        this.salesDate = salesDate;
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
