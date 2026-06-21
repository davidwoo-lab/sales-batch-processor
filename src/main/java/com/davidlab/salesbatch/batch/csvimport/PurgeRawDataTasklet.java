package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.repository.SalesRawDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 재적재 멱등성을 위한 선행 Tasklet.
 * targetDate가 주어지면 해당 일자의 기존 원본 데이터를 먼저 삭제한 뒤 적재하도록 한다.
 * 동일 파일을 다시 적재해도 데이터가 중복되지 않도록 보장한다.
 * targetDate가 없으면(신규 적재) 삭제를 건너뛴다.
 */
@Slf4j
@Component
@StepScope
public class PurgeRawDataTasklet implements Tasklet {

    private final SalesRawDataRepository rawDataRepository;
    private final String targetDate;

    public PurgeRawDataTasklet(SalesRawDataRepository rawDataRepository,
                               @Value("#{jobParameters['targetDate']}") String targetDate) {
        this.rawDataRepository = rawDataRepository;
        this.targetDate = targetDate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        if (!StringUtils.hasText(targetDate)) {
            log.info("targetDate가 없어 기존 데이터 삭제를 건너뜁니다. (신규 적재)");
            return RepeatStatus.FINISHED;
        }
        LocalDate date = LocalDate.parse(targetDate);
        int deleted = rawDataRepository.deleteByOrderDate(date);
        log.info("재적재 멱등성: {} 일자 기존 데이터 {}건 삭제", date, deleted);
        return RepeatStatus.FINISHED;
    }
}
