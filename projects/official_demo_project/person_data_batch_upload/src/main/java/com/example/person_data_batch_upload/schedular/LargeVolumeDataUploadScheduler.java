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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component(value = "largeVolumeDataUploadScheduler")
@Slf4j
public class LargeVolumeDataUploadScheduler {

    private final JobLauncher jobLauncher;

    private final Job largeVolumeDataUploadJob;
    private final JobExplorer jobExplorer;
    private boolean hasExecuted = false;

    public LargeVolumeDataUploadScheduler(JobLauncher jobLauncher,
                                          @Qualifier("importMenuDataJob") Job largeVolumeDataUploadJob,
                                          JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.largeVolumeDataUploadJob = largeVolumeDataUploadJob;
        this.jobExplorer = jobExplorer;
    }

    // todo: Uncomment when required
//    @Scheduled(cron = "16 22 * * 1 *")
//    public void scheduleUploadLargeVolumeMenuData() throws JobInstanceAlreadyCompleteException,
//            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("jobName", "importMenuDataJob")
//                .addLong("importedAt", System.currentTimeMillis())
//                .toJobParameters();
//        jobLauncher.run(largeVolumeDataUploadJob, jobParameters);
//        log.info("............... Import Menu data Scheduler started .............");
//
//        hasExecuted = true;
//    }

    public boolean hasExecuted() {
        return hasExecuted;
    }
}
