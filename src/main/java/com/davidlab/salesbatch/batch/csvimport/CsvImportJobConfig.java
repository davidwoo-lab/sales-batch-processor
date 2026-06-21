package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.common.config.BatchProperties;
import com.davidlab.salesbatch.sales.domain.SalesRawData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job 1 — csvImportJob 설정.
 * CSV 파일을 청크 단위로 읽어 검증 후 매출 원본 테이블에 적재한다.
 */
@Configuration
@RequiredArgsConstructor
public class CsvImportJobConfig {

    private final BatchProperties batchProperties;

    /**
     * CSV 파일 Reader. Job 파라미터 filePath로 대상 파일을 주입받는다.
     * 헤더 1줄을 건너뛰고, 파싱 오류는 FlatFileParseException으로 전달되어 Step에서 skip된다.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<SalesCsvDto> salesCsvItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<SalesCsvDto>()
                .name("salesCsvItemReader")
                .resource(new FileSystemResource(filePath))
                .encoding("UTF-8") // 한글 상품명이 환경에 무관하게 보존되도록 인코딩 명시
                .linesToSkip(1) // 헤더 라인 건너뜀
                .delimited()
                .names("orderDate", "storeId", "productName", "quantity", "unitPrice", "totalAmount")
                .fieldSetMapper(new SalesCsvFieldSetMapper())
                .build();
    }

    /**
     * CSV 적재 Step (Chunk 기반).
     * chunk size와 skip limit은 설정값(BatchProperties)으로 조정한다.
     */
    @Bean
    public Step csvImportStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              FlatFileItemReader<SalesCsvDto> salesCsvItemReader,
                              SalesDataValidator processor,
                              SalesRawDataItemWriter writer) {
        return new StepBuilder("csvImportStep", jobRepository)
                .<SalesCsvDto, SalesRawData>chunk(batchProperties.chunkSize(), transactionManager)
                .reader(salesCsvItemReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(batchProperties.skipLimit())
                .skip(FlatFileParseException.class)
                .build();
    }

    /**
     * 재적재 멱등성 확보용 선행 Step.
     * targetDate가 주어지면 해당 일자의 기존 데이터를 삭제한 후 적재한다.
     */
    @Bean
    public Step purgeRawDataStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 PurgeRawDataTasklet purgeRawDataTasklet) {
        return new StepBuilder("purgeRawDataStep", jobRepository)
                .tasklet(purgeRawDataTasklet, transactionManager)
                .build();
    }

    /**
     * CSV 적재 Job. 기존 데이터 삭제(purge) → CSV 적재 순으로 실행한다.
     */
    @Bean
    public Job csvImportJob(JobRepository jobRepository, Step purgeRawDataStep, Step csvImportStep) {
        return new JobBuilder("csvImportJob", jobRepository)
                .start(purgeRawDataStep)
                .next(csvImportStep)
                .build();
    }
}
