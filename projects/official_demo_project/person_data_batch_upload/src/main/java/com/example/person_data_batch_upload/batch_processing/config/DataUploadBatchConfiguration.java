package com.example.person_data_batch_upload.batch_processing.config;

import com.example.person_data_batch_upload.batch_processing.JobListener;
import com.example.person_data_batch_upload.batch_processing.MenuDataItemProcessor;
import com.example.person_data_batch_upload.batch_processing.ShopDataItemProcessor;
import com.example.person_data_batch_upload.batch_processing.reader.MenuDataItemReader;
import com.example.person_data_batch_upload.batch_processing.row_mapper.MenuDataRowMapper;
import com.example.person_data_batch_upload.batch_processing.row_mapper.ShopDataRowMapper;
import com.example.person_data_batch_upload.batch_processing.writer.MenuDataItemWriter;
import com.example.person_data_batch_upload.batch_processing.writer.ShopDataItemWriter;
import com.example.person_data_batch_upload.model.entity.MenuData;
import com.example.person_data_batch_upload.model.entity.Shop;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Data
@Slf4j
public class DataUploadBatchConfiguration {

    private final DataSource dataSource;
    private final MenuDataRowMapper menuDataRowMapper;
    private final ShopDataRowMapper shopDataRowMapper;
    private final JobExplorer jobExplorer;
    private final ExecutionContext executionContext;
    private final MenuDataItemWriter menuDataItemWriter;


    @Value("${menu_data.chunk.size:10}")
    private Integer menuDataChunkSize;

    @Value("${shop_data.chunk.size:2}")
    private Integer shopDataChunkSize;


    @Bean(name = "shopDataReader")
    public JdbcCursorItemReader<Shop> shopDataItemReader() {

        final String SHOP_MENU_SQL = "select shop_id from shop where is_private=false";

        JdbcCursorItemReader<Shop> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("shopDataReader");
        // collect data
        reader.setSql(SHOP_MENU_SQL);
        reader.setRowMapper(shopDataRowMapper);
        // same as setPageSize() method of EntityManagerFactory class
        // todo: We don't need fetch size here
        // collect each shop and send one
//        reader.setFetchSize(chunkSize);
        return reader;
    }

    @Bean(name = "shopDataImportWriter")
    public ItemWriter<Shop> shopDataImportWriter() {
//        return new ItemWriter<>() {
//            private StepExecution stepExecution;
//
//            @Override
//            public void write(Chunk<? extends Shop> chunk) throws Exception {
//                ExecutionContext context = this.stepExecution.getExecutionContext();
//                context.put("shopId", chunk.getItems());
//                log.info("Getting the shopId from context: {}", context.get("shopId"));
//            }
//        };
        // todo: fix this line
//        stepExecution.getExecutionContext().put("totalCount", 1);
//        log.info("Total count: {}", stepExecution.getExecutionContext().get("totalCount"));
//        return shops -> {
//            for (Shop shop : shops) {
//                System.out.println("Collected Shops: " + shop);
//            }
//        };

        return new ShopDataItemWriter();
    }

    // todo: Add shop exists or not and public or not step
    @Bean(name = "menuDataImportReader")
    public JdbcCursorItemReader<MenuData> menuDataImportReader() {
        log.info("In the menu data import reader:{}", (List<Shop>) executionContext.get("shops"));
        final String MENU_INFO_FROM_SERVICE_BUDGET_AND_MENU_IMAGES_TABLE = """
                select sb.menu_text,
                       sb.service_budget_id,
                       sb.shop_id,
                       mi.menu_images_cloud_data
                from service_budget sb
                         left join menu_images mi on
                    (sb.service_id = mi.service_id and sb.shop_id = mi.shop_id)
                    where sb.shop_id='1'
                    """;

        JdbcCursorItemReader<MenuData> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("menuDataImportReader");
        // collect data
        reader.setSql(MENU_INFO_FROM_SERVICE_BUDGET_AND_MENU_IMAGES_TABLE);
        reader.setRowMapper(menuDataRowMapper);
        // same as setPageSize() method of EntityManagerFactory class
        reader.setFetchSize(menuDataChunkSize);
        return reader;
//        return new MenuDataItemReader(dataSource, menuDataRowMapper, 1);
//        return new MenuDataItemReader(dataSource, menuDataRowMapper);
    }


    @Bean
    public ItemProcessor<MenuData, MenuData> menuDataImportProcessor() {
        return new MenuDataItemProcessor();
    }

//    @Bean(name = "menuDataImportWriter")
//    public JdbcBatchItemWriter<MenuData> menuDataImportWriter() {
//        // todo: Uncomment it after usage
//        final String INSERT_SQL = "INSERT INTO menu_data(menu_text, service_budget_id, shop_id, last_modify_date, menu_images_cloud_data) VALUES (:menuText, :serviceBudgetId, :shopId, :lastModifyDate, :menuImagesCloudData)";
//        return new JdbcBatchItemWriterBuilder<MenuData>()
//                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
//                .sql(INSERT_SQL)
//                .dataSource(dataSource)
//                .build();
//
//        // todo: Call the API here
//    }


    // todo: Send using @PostConstruct
    @Bean
    public JdbcCursorItemReader<MenuData> updateAndInsertSmallVolumeMenuDataReader() {
        // todo: Check what if importMenuDataJob is not run
//        final String lastModifyDate = "2023-04-12 07:03:09.785452";
        JobInstance importMenuDataJobInstance = jobExplorer.getLastJobInstance("importMenuDataJob");
        JobInstance updateMenuDataJobInstance = jobExplorer.getLastJobInstance("updateMenuDataJob");
        // todo: It will throw null pointer exception if data is not imported yet
        LocalDateTime importMenuDataJobStartTime = jobExplorer.getLastJobInstance("`importMenuDataJob`") == null
                ? LocalDateTime.now() : jobExplorer.getJobExecution(importMenuDataJobInstance.getInstanceId()).getStartTime();

        log.info("importMenuDataJobStartTime: {}", importMenuDataJobStartTime);
        log.info("updateMenuDataJobInstance: {}", updateMenuDataJobInstance);

        String lastModifyDate = updateMenuDataJobInstance == null
                ? String.valueOf(importMenuDataJobStartTime)
                : String.valueOf(jobExplorer.getJobExecution(jobExplorer.getLastJobInstance("updateMenuDataJob").getInstanceId()).getStartTime());

        log.info("Last modify date: {}", lastModifyDate);
        JdbcCursorItemReader<MenuData> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("updateAndInsertSmallVolumeMenuDataReader");
        // todo: startsAt of importMenuDataJob, startsAt of updateMenuDataJob
        reader.setSql(
                String.format("""
                        SELECT sb.menu_text,
                               sb.service_budget_id,
                               sb.shop_id,
                               mi.menu_images_cloud_data
                        from service_budget sb
                                 left join menu_images mi on sb.service_id = mi.service_id and sb.shop_id = mi.shop_id
                        where last_modify_date > '%s'""", lastModifyDate)
        );

        reader.setRowMapper(menuDataRowMapper);
        reader.setFetchSize(menuDataChunkSize);
        return reader;
    }

    @Bean(name = "personUpdateWriter")
    public JdbcBatchItemWriter<MenuData> personUpdateWriter() {
        return new JdbcBatchItemWriterBuilder<MenuData>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("Update menu_data SET menu_text = :menuText, menu_images_cloud_data=:menuImagesCloudData where shop_id=:shopId and service_budget_id=:serviceBudgetId")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemProcessor<Shop, Shop> shopDataImportProcessor() {
        return new ShopDataItemProcessor();
    }

    @Bean(name = "shopDataImportStep")
    public Step shopDataImportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("shopImportStep", jobRepository).
                <Shop, Shop>chunk(shopDataChunkSize, transactionManager)
                .reader(shopDataItemReader())
                .processor(shopDataImportProcessor())
                .writer(shopDataImportWriter())
                .build();
    }


    @Bean(name = "menuDataImportStep")
    public Step menuDataImportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("menuDataImportStep", jobRepository).
                <MenuData, MenuData>chunk(menuDataChunkSize, transactionManager)
                .reader(menuDataImportReader())
                .processor(menuDataImportProcessor())
                .writer(menuDataItemWriter)
                .build();
    }

    @Bean(name = "menuDataUpdateStep")
    public Step menuDataUpdateStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("menuDataUpdateStep", jobRepository).
                <MenuData, MenuData>chunk(menuDataChunkSize, transactionManager)
                .reader(updateAndInsertSmallVolumeMenuDataReader()) // todo: remove null from here
                .processor(menuDataImportProcessor())
                .writer(personUpdateWriter())
                .build();
    }

    @Bean(name = "importMenuDataJob")
    public Job importMenuDataJob(JobRepository jobRepository,
                                 JobListener listener,
                                 @Qualifier("menuDataImportStep") Step menuDataImportStep,
                                 @Qualifier("shopDataImportStep") Step shopDataImportStep
    ) {
        return new JobBuilder("importMenuDataJob", jobRepository)

                .incrementer(new RunIdIncrementer())
                .start(shopDataImportStep)
                .next(menuDataImportStep)
                .build();
    }

    // update job
    @Bean(name = "updateMenuDataJob")
    public Job updateMenuDataJob(JobRepository jobRepository,
                                 JobListener listener,
                                 @Qualifier(value = "menuDataUpdateStep") Step step) {
        return new JobBuilder("updateMenuDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }
}
