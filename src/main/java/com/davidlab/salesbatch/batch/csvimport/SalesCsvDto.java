package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.domain.SalesRawData;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * CSV 한 줄을 매핑하는 DTO.
 * 입력 포맷: order_date, store_id, product_name, quantity, unit_price, total_amount
 */
@Getter
@Builder
public class SalesCsvDto {

    private final LocalDate orderDate;
    private final String storeId;
    private final String productName;
    private final int quantity;
    private final long unitPrice;
    private final long totalAmount;

    /**
     * 검증을 통과한 DTO를 매출 원본 Entity로 변환한다.
     */
    public SalesRawData toEntity() {
        return SalesRawData.builder()
                .orderDate(orderDate)
                .storeId(storeId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .build();
    }
}
