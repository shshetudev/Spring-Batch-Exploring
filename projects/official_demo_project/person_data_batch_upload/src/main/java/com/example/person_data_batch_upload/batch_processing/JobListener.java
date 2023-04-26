package com.example.person_data_batch_upload.batch_processing;

import com.example.person_data_batch_upload.model.entity.MenuData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
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
        final String CHANGED_COLUMNS = "1";
        Long importMenuDataJobInstanceId = jobExplorer.getLastJobInstance("importMenuDataJob").getInstanceId();
        LocalDateTime importMenuDataJobStartTime = jobExplorer.getJobExecution(importMenuDataJobInstanceId).getStartTime();
        log.info("importMenuDataJobStartTime: {}", importMenuDataJobStartTime);
//        Long updateMenuDataJobInstanceId = jobExplorer.getLastJobInstance("updateMenuDataJob").getInstanceId();
        // Get the last startsAt parameter
        // todo: Check will be done based on both importMenuDataJobStartTime and updateMenuDataJobStartTime
//        LocalDateTime updateMenuDataJobStartTime = jobExplorer.getJobExecution(updateMenuDataJobInstanceId).getStartTime();
//        log.info("updateMenuDataJobStartTime: {}", updateMenuDataJobStartTime);

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");
            final String SEARCH_QUERY = "SELECT md.menu_text, md.service_budget_id, md.shop_id, md.last_modify_date, md.menu_images_cloud_data " +
                    "FROM menu_data md inner join service_budget sb on sb.shop_id=md.shop_id and sb.shop_id=md.service_budget_id " +
                    "order by md.last_modify_date limit " + CHANGED_COLUMNS;
            jdbcTemplate.query(SEARCH_QUERY,
                    (rs, row) -> new MenuData(
                            rs.getString(1),
                            rs.getInt(2),
                            rs.getInt(3),
                            rs.getLong(4),
                            rs.getString(5)

                    )).forEach(menuData -> log.info("Found {} in the database.", menuData));
        }
    }
}
