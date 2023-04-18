package com.example.person_data_batch_upload.schedular;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DataUploadScheduler {
    private final JobLauncher jobLauncher;

    private final Job importPersonJob;
    private final Job updatePersonJob;
    private final JobExplorer jobExplorer;

    public DataUploadScheduler(JobLauncher jobLauncher,
                               @Qualifier("importUserJob") Job importPersonJob,
                               @Qualifier("updateUserJob") Job updatePersonJob, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.importPersonJob = importPersonJob;
        this.updatePersonJob = updatePersonJob;
        this.jobExplorer = jobExplorer;
    }

    /**
     * * * * * *
     * | | | | | |
     * | | | | | +-- Year              (range: 1900-3000)
     * | | | | +---- Day of the Week   (range: 1-7, 1 standing for Monday)
     * | | | +------ Month of the Year (range: 1-12)
     * | | +-------- Day of the Month  (range: 1-31)
     * | +---------- Hour              (range: 0-23)
     * +------------ Minute            (range: 0-59)
     */
    // <minute> <hour> <day-of-month> <month> <day-of-week>

    // todo: Uncomment to run this scheduler
//    @Scheduled(fixedRate = 1000)
    public void demoTask() {
        log.info("Running this dummy scheduler after every second");
    }

    @Scheduled(cron = "16 22 * * * *")
    public void scheduleUploadLargeVolumeMenuData() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "importMenuDataJob")
                .addLong("importedAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importPersonJob, jobParameters);
        log.info("............... Import Menu data Scheduler started .............");
    }

    @DependsOn("scheduleUploadUserData")
    @Scheduled(fixedRate = 1800_000)
    public void scheduleUpdateAndInsertSmallVolumeMenuData() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "updateUserJob")
                .addLong("updatedAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(updatePersonJob, jobParameters);
        log.info("............... Update User Scheduler started .............");
    }
}
