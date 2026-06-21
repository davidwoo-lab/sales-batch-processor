package com.davidlab.salesbatch.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 배치 동작 관련 설정값.
 * 대용량 대비 청크 사이즈/skip 한도를 설정으로 분리해 코드 수정 없이 조정 가능하게 한다.
 *
 * @param chunkSize CSV 적재 청크 단위 (기본 100)
 * @param skipLimit CSV 파싱 오류 허용 건수 (기본 50)
 */
@ConfigurationProperties(prefix = "batch.sales")
public record BatchProperties(int chunkSize, int skipLimit) {

    public BatchProperties {
        // 설정값 미지정 시 기본값 적용
        if (chunkSize <= 0) {
            chunkSize = 100;
        }
        if (skipLimit < 0) {
            skipLimit = 50;
        }
    }
}
