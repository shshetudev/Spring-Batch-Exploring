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

@Component
@Slf4j
public class SmallVolumeDataUploadScheduler {
    private final LargeVolumeDataUploadScheduler largeVolumeDataUploadScheduler;
    private final Job smallVolumeDataUploadJob;
    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;

    public SmallVolumeDataUploadScheduler(LargeVolumeDataUploadScheduler largeVolumeDataUploadScheduler,
                                          @Qualifier("updateMenuDataJob")Job smallVolumeDataUploadJob,
                                          JobExplorer jobExplorer,
                                          JobLauncher jobLauncher) {
        this.largeVolumeDataUploadScheduler = largeVolumeDataUploadScheduler;
        this.smallVolumeDataUploadJob = smallVolumeDataUploadJob;
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
    }

    // todo: Uncomment when required
//    @Scheduled(fixedRate = 1800_000)
//    public void scheduleUpdateAndInsertSmallVolumeMenuData() throws JobInstanceAlreadyCompleteException,
//            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
//        if (largeVolumeDataUploadScheduler.hasExecuted()) {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addString("jobName", "updateMenuDataJob")
//                    .addLong("updatedAt", System.currentTimeMillis())
//                    .toJobParameters();
//            jobLauncher.run(smallVolumeDataUploadJob, jobParameters);
//            log.info("............... Update User Scheduler started .............");
//        }
//    }
}
