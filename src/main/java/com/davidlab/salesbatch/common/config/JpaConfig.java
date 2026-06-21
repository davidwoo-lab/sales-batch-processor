package com.davidlab.salesbatch.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정.
 * BaseEntity의 createdAt/updatedAt 자동 기록을 위해 Auditing을 활성화한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
