package com.davidlab.salesbatch.batch.csvimport;

import com.davidlab.salesbatch.sales.domain.SalesRawData;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.stereotype.Component;

/**
 * 매출 원본 데이터를 청크 단위로 DB에 저장하는 Writer.
 * 내부적으로 JpaItemWriter에 위임한다.
 */
@Component
public class SalesRawDataItemWriter implements ItemWriter<SalesRawData> {

    private final JpaItemWriter<SalesRawData> delegate;

    public SalesRawDataItemWriter(EntityManagerFactory entityManagerFactory) {
        this.delegate = new JpaItemWriterBuilder<SalesRawData>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Override
    public void write(Chunk<? extends SalesRawData> chunk) throws Exception {
        delegate.write(chunk);
    }
}
