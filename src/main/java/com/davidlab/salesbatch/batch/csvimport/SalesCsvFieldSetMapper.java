package com.davidlab.salesbatch.batch.csvimport;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.time.LocalDate;

/**
 * CSV FieldSet을 SalesCsvDto로 변환하는 매퍼.
 * 날짜/숫자 파싱 오류 시 예외가 발생하며, FlatFileItemReader가 이를 FlatFileParseException으로
 * 감싸 Step의 skip 대상으로 전달한다.
 */
public class SalesCsvFieldSetMapper implements FieldSetMapper<SalesCsvDto> {

    @Override
    public SalesCsvDto mapFieldSet(FieldSet fieldSet) {
        return SalesCsvDto.builder()
                .orderDate(LocalDate.parse(fieldSet.readString("orderDate")))
                .storeId(fieldSet.readString("storeId"))
                .productName(fieldSet.readString("productName"))
                .quantity(fieldSet.readInt("quantity"))
                .unitPrice(fieldSet.readLong("unitPrice"))
                .totalAmount(fieldSet.readLong("totalAmount"))
                .build();
    }
}
