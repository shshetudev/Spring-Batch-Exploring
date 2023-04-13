package com.example.person_data_batch_upload.batch_processing.config;

import com.example.person_data_batch_upload.batch_processing.JobListener;
import com.example.person_data_batch_upload.batch_processing.PersonItemProcessor;
import com.example.person_data_batch_upload.model.entity.Person;
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
    private final PersonRowMapper rowMapper;
    private final JobExplorer jobExplorer;

//    @Value("#{jobParameters['importedAt']}")
//    public String lastModifyDate;


    @Value("${chunk.size:10}")
    private Integer chunkSize;

    @Bean
    public JdbcCursorItemReader<Person> personImportReader() {


        JdbcCursorItemReader<Person> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setName("personImportReader");
        reader.setSql("SELECT first_name, last_name, service_id, shop_id from service_budget");
        reader.setRowMapper(rowMapper);
        // same as setPageSize() method of EntityManagerFactory class
        reader.setFetchSize(chunkSize);
        return reader;
    }


    @Bean
    public ItemProcessor<Person, Person> personImportProcessor() {
        return new PersonItemProcessor();
    }

    @Bean(name = "personImportWriter")
    public JdbcBatchItemWriter<Person> personImportWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO person(first_name, last_name, service_id, shop_id) VALUES (:firstName, :lastName, :serviceId, :shopId)")
                .dataSource(dataSource)
                .build();
    }


    // todo: Send using @PostConstruct
    @Bean
    public JdbcCursorItemReader<Person> personUpdateReader() {
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
        JdbcCursorItemReader<Person> reader = new JdbcCursorItemReader<>();
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

        reader.setRowMapper(rowMapper);
        reader.setFetchSize(chunkSize);
        return reader;
    }

    @Bean(name = "personUpdateWriter")
    public JdbcBatchItemWriter<Person> personUpdateWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("Update person SET first_name = :firstName, last_name=:lastName where shop_id=:shopId and service_id=:serviceId")
                .dataSource(dataSource)
                .build();
    }

    @Bean(name = "personImportStep")
    public Step personImportStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("personImportStep", jobRepository).
                <Person, Person>chunk(chunkSize, transactionManager)
                .reader(personImportReader())
                .processor(personImportProcessor())
                .writer(personImportWriter())
                .build();
    }

    @Bean(name = "personUpdateStep")
    public Step personUpdateStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("personUpdateStep", jobRepository).
                <Person, Person>chunk(chunkSize, transactionManager)
                .reader(personUpdateReader()) // todo: remove null from here
                .processor(personImportProcessor())
                .writer(personUpdateWriter())
                .build();
    }

    @Bean(name = "importUserJob")
    public Job importUserJob(JobRepository jobRepository,
                             JobListener listener,
                             @Qualifier("personImportStep") Step step) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
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
}
