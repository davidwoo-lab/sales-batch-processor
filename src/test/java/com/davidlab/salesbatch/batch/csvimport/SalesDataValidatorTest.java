package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.domain.SalesRawData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SalesDataValidator(Processor) 단위 테스트.
 * 검증 실패 시 null(skip), 정상 데이터는 Entity로 변환되는지 확인한다.
 */
class SalesDataValidatorTest {

    private final SalesDataValidator validator = new SalesDataValidator();

    @Test
    @DisplayName("정상 데이터는 Entity로 변환된다")
    void process_validData_returnsEntity() {
        SalesCsvDto dto = SalesCsvDto.builder()
                .orderDate(LocalDate.of(2026, 6, 1))
                .storeId("STORE001")
                .productName("아메리카노")
                .quantity(3)
                .unitPrice(4500)
                .totalAmount(13500)
                .build();

        SalesRawData result = validator.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isEqualTo("STORE001");
        assertThat(result.getTotalAmount()).isEqualTo(13500);
    }

    @Test
    @DisplayName("수량이 음수면 skip(null) 처리된다")
    void process_negativeQuantity_returnsNull() {
        SalesCsvDto dto = SalesCsvDto.builder()
                .orderDate(LocalDate.of(2026, 6, 1))
                .storeId("STORE001")
                .productName("아메리카노")
                .quantity(-1)
                .unitPrice(4500)
                .totalAmount(13500)
                .build();

        assertThat(validator.process(dto)).isNull();
    }

    @Test
    @DisplayName("매장 ID가 비어 있으면 skip(null) 처리된다")
    void process_blankStoreId_returnsNull() {
        SalesCsvDto dto = SalesCsvDto.builder()
                .orderDate(LocalDate.of(2026, 6, 1))
                .storeId("  ")
                .productName("아메리카노")
                .quantity(3)
                .unitPrice(4500)
                .totalAmount(13500)
                .build();

        assertThat(validator.process(dto)).isNull();
    }
}
