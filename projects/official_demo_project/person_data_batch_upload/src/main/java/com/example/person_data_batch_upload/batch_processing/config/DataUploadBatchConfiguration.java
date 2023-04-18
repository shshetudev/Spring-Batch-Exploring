package com.example.person_data_batch_upload.batch_processing.config;

import com.example.person_data_batch_upload.batch_processing.JobListener;
import com.example.person_data_batch_upload.batch_processing.MenuDataItemProcessor;
import com.example.person_data_batch_upload.batch_processing.ShopDataItemProcessor;
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

@Configuration
@EnableBatchProcessing
@Data
@Slf4j
public class DataUploadBatchConfiguration {

    private final DataSource dataSource;
    private final MenuDataRowMapper menuDataRowMapper;
    private final ShopDataRowMapper shopDataRowMapper;
    private final JobExplorer jobExplorer;

//    @Value("#{jobParameters['importedAt']}")
//    public String lastModifyDate;


    @Value("${chunk.size:10}")
    private Integer chunkSize;

    // todo: Add shop exists or not and public or not step
    @Bean(name = "menuDataImportReader")
    public JdbcCursorItemReader<MenuData> menuDataImportReader() {

        final String MENU_INFO_FROM_SERVICE_BUDGET_AND_MENU_IMAGES_TABLE = """
                select sb.menu_text,
                       sb.service_budget_id,
                       sb.shop_id,
                       mi.menu_images_cloud_data
                from service_budget sb
                         left join menu_images mi on
                    (sb.service_id = mi.service_id and sb.shop_id = mi.shop_id)""";

        JdbcCursorItemReader<MenuData> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("menuDataImportReader");
        // collect data
        reader.setSql(MENU_INFO_FROM_SERVICE_BUDGET_AND_MENU_IMAGES_TABLE);
        reader.setRowMapper(menuDataRowMapper);
        // same as setPageSize() method of EntityManagerFactory class
        reader.setFetchSize(chunkSize);
        return reader;
    }

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


    @Bean
    public ItemProcessor<MenuData, MenuData> menuDataImportProcessor() {
        return new MenuDataItemProcessor();
    }

    @Bean(name = "menuDataImportWriter")
    public JdbcBatchItemWriter<MenuData> menuDataImportWriter() {
        final String INSERT_SQL = "INSERT INTO menu_data(menu_text, service_budget_id, shop_id, last_modify_date, menu_images_cloud_data) VALUES (:menuText, :serviceBudgetId, :shopId, :lastModifyDate, :menuImagesCloudData)";
        return new JdbcBatchItemWriterBuilder<MenuData>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(INSERT_SQL)
                .dataSource(dataSource)
                .build();
    }

    @Bean(name = "shopDataImportWriter")
    public ItemWriter<Shop> shopDataImportWriter() {
        return shops -> {
            for (Shop shop : shops) {
                System.out.println(shop);
            }
        };
    }


    // todo: Send using @PostConstruct
    @Bean
    public JdbcCursorItemReader<MenuData> updateAndInsertSmallVolumeMenuDataReader() {
        // todo: Check what if importUserJob is not run
//        final String lastModifyDate = "2023-04-12 07:03:09.785452";
        JobInstance importUserJobInstance = jobExplorer.getLastJobInstance("importUserJob");
        JobInstance updateUserJobInstance = jobExplorer.getLastJobInstance("updateUserJob");
        LocalDateTime importUserJobStartTime = jobExplorer.getJobExecution(importUserJobInstance.getInstanceId()).getStartTime();

        log.info("importUserJobStartTime: {}", importUserJobStartTime);
        log.info("updateUserJobInstance: {}", updateUserJobInstance);

        String lastModifyDate = updateUserJobInstance == null
                ? String.valueOf(jobExplorer.getJobExecution(jobExplorer.getLastJobInstance("importUserJob").getInstanceId()).getStartTime())
                : String.valueOf(jobExplorer.getJobExecution(jobExplorer.getLastJobInstance("updateUserJob").getInstanceId()).getStartTime());

        log.info("Last modify date: {}", lastModifyDate);
        JdbcCursorItemReader<MenuData> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("personUpdateReader");
        // todo: startsAt of importUserJob, startsAt of updateUserJob
        reader.setSql(
                String.format("SELECT " +
                        "first_name, last_name, service_id, shop_id " +
                        "from service_budget " +
                        "where last_modify_date > '%s'", lastModifyDate)
        );


//        reader.setSql(
//                "SELECT first_name, last_name, service_id, shop_id from service_budget " +
//                "where last_modify_date > ?");
//        reader.setPreparedStatementSetter(new ParameterizedPreparedStatementSetter<Person>() {
//            @Override
//            public void setValues(PreparedStatement ps, Person person) throws SQLException {
//                ps.setString();
//            }
//        });

        reader.setRowMapper(menuDataRowMapper);
        reader.setFetchSize(chunkSize);
        return reader;
    }

    @Bean(name = "personUpdateWriter")
    public JdbcBatchItemWriter<MenuData> personUpdateWriter() {
        return new JdbcBatchItemWriterBuilder<MenuData>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("Update person SET first_name = :firstName, last_name=:lastName where shop_id=:shopId and service_id=:serviceId")
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
                <Shop, Shop> chunk(chunkSize, transactionManager)
                .reader(shopDataItemReader())
                .processor(shopDataImportProcessor())
                .writer(shopDataImportWriter())
                .build();
    }


    @Bean(name = "menuDataImportStep")
    public Step menuDataImportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("menuDataImportStep", jobRepository).
                <MenuData, MenuData>chunk(chunkSize, transactionManager)
                .reader(menuDataImportReader())
                .processor(menuDataImportProcessor())
                .writer(menuDataImportWriter())
                .build();
    }

    @Bean(name = "personUpdateStep")
    public Step personUpdateStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("personUpdateStep", jobRepository).
                <MenuData, MenuData>chunk(chunkSize, transactionManager)
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
        return new JobBuilder("importUserJob", jobRepository)

                .incrementer(new RunIdIncrementer())
                .start(shopDataImportStep)
                .next(menuDataImportStep)
                .build();
    }

    // update job
    @Bean(name = "updateUserJob")
    public Job updateUserJob(JobRepository jobRepository,
                             JobListener listener,
                             @Qualifier(value = "personUpdateStep") Step step) {
        return new JobBuilder("updateUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    // todo: Fix the dummy step
//    @Bean
//    public Step taskletStep() {
//        return new StepBuilder.get("taskletStep")
//                .tasklet(tasklet())
//                .build();
//    }
}
