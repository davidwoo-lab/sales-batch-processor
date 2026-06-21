package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.domain.SalesRawData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * CSV 매출 데이터를 검증하고 Entity로 변환하는 Processor.
 * 필수 컬럼 누락이나 형식 오류 시 null을 반환해 skip 대상으로 처리한다.
 */
@Slf4j
@Component
public class SalesDataValidator implements ItemProcessor<SalesCsvDto, SalesRawData> {

    /**
     * CSV 한 줄을 검증 후 SalesRawData로 변환한다.
     *
     * @param dto CSV에서 읽은 원본 데이터
     * @return 변환된 매출 원본 Entity, 검증 실패 시 null 반환하여 skip 처리
     */
    @Override
    public SalesRawData process(SalesCsvDto dto) {
        // 필수 문자열(매장/상품) 누락 검증
        if (!StringUtils.hasText(dto.getStoreId()) || !StringUtils.hasText(dto.getProductName())) {
            log.warn("필수 항목 누락으로 skip: storeId={}, productName={}", dto.getStoreId(), dto.getProductName());
            return null;
        }
        // 수량/단가/금액이 음수면 잘못된 데이터로 간주하고 건너뜀
        if (dto.getQuantity() < 0 || dto.getUnitPrice() < 0 || dto.getTotalAmount() < 0) {
            log.warn("음수 값으로 skip: storeId={}, quantity={}, unitPrice={}, totalAmount={}",
                    dto.getStoreId(), dto.getQuantity(), dto.getUnitPrice(), dto.getTotalAmount());
            return null;
        }
        return dto.toEntity();
    }
}
