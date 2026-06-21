package com.davidlab.salesbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 매출 배치 처리 애플리케이션 진입점.
 * Spring Batch는 Boot 자동 설정에 위임하며, 스케줄러 트리거를 위해 스케줄링을 활성화한다.
 */
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class SalesBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesBatchApplication.class, args);
    }
}
