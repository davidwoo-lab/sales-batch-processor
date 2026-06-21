package com.davidlab.salesbatch.sales.domain;

import com.davidlab.salesbatch.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CSV로 적재된 매출 원본 데이터.
 * 집계(Job 2)의 입력이 되며, 조회 성능을 위해 (order_date, store_id) 인덱스를 둔다.
 */
@Getter
@Entity
@Table(name = "sales_raw_data", indexes = {
        @Index(name = "idx_raw_order_date_store", columnList = "order_date, store_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesRawData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 주문 일자 */
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    /** 매장 식별자 */
    @Column(name = "store_id", nullable = false, length = 50)
    private String storeId;

    /** 상품명 */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** 판매 수량 */
    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** 단가 (원) */
    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    /** 총 금액 (원) */
    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Builder
    private SalesRawData(LocalDate orderDate, String storeId, String productName,
                         int quantity, long unitPrice, long totalAmount) {
        this.orderDate = orderDate;
        this.storeId = storeId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
    }
}
