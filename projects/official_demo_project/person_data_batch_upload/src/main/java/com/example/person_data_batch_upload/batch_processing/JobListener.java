package com.example.person_data_batch_upload.batch_processing;

import com.example.person_data_batch_upload.model.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class JobListener implements JobExecutionListener {
    private final JdbcTemplate jdbcTemplate;
    private final JobExplorer jobExplorer;

    public JobListener(JdbcTemplate jdbcTemplate, JobExplorer jobExplorer) {
        this.jdbcTemplate = jdbcTemplate;
        this.jobExplorer = jobExplorer;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.STARTED) {
            log.info("Person import job started...!");
        }
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long importUserJobInstanceId = jobExplorer.getLastJobInstance("importUserJob").getInstanceId();
        LocalDateTime importUserJobStartTime = jobExplorer.getJobExecution(importUserJobInstanceId).getStartTime();
        log.info("importUserJobStartTime: {}", importUserJobStartTime);
//        Long updateUserJobInstanceId = jobExplorer.getLastJobInstance("updateUserJob").getInstanceId();
        // Get the last startsAt parameter
        // todo: Check will be done based on both importUserJobStartTime and updateUserJobStartTime
//        LocalDateTime updateUserJobStartTime = jobExplorer.getJobExecution(updateUserJobInstanceId).getStartTime();
//        log.info("updateUserJobStartTime: {}", updateUserJobStartTime);

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");
            jdbcTemplate.query("SELECT first_name, last_name, service_id, shop_id FROM person",
                    (rs, row) -> new Person(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getInt(3),
                            rs.getInt(4)
                    )).forEach(person -> log.info("Found {} in the database.", person));
        }
    }
}
