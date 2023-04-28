package com.example.person_data_batch_upload.batch_processing.writer;

import com.example.person_data_batch_upload.model.entity.Shop;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Slf4j
public class ShopDataItemWriter implements ItemWriter<Shop>, StepExecutionListener {
    private List<Shop> shops;
    @Override
    public void write(Chunk<? extends Shop> chunk) throws Exception {
        log.info("Printing from ShopDataItemWriter");
        chunk.forEach(System.out::println);
        shops = (List<Shop>) chunk.getItems();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext()
                .put("shops", shops);
        return StepExecutionListener.super.afterStep(stepExecution);
    }
}
