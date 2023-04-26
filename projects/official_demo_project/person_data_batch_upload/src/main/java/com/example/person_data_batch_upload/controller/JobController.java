package com.example.person_data_batch_upload.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobLauncher jobLauncher;
    private final Job importPersonJob;
    private final Job updatePersonJob;

    public JobController(JobLauncher jobLauncher,
                         @Qualifier("importMenuDataJob") Job importPersonJob,
                         @Qualifier("updateMenuDataJob") Job updatePersonJob) {
        this.jobLauncher = jobLauncher;
        this.importPersonJob = importPersonJob;
        this.updatePersonJob = updatePersonJob;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importPersons() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "importMenuDataJob")
                .addLong("startsAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importPersonJob, jobParameters);
        return ResponseEntity.ok("Import job started");
    }

    @PostMapping("/update")
    public ResponseEntity<String> updatePersons() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "updateMenuDataJob")
                .addLong("updatedAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(updatePersonJob, jobParameters);
        return ResponseEntity.ok("Update job started");
    }
}
