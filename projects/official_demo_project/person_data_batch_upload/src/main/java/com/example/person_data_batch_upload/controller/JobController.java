package com.example.person_data_batch_upload.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobLauncher jobLauncher;
    private final Job importPersonJob;

    public JobController(JobLauncher jobLauncher, Job importPersonJob) {
        this.jobLauncher = jobLauncher;
        this.importPersonJob = importPersonJob;
    }

    @PostMapping("/start")
    public void startJob() throws IOException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        String fakeFile = """
                Fake,Doe
                Bake,Doe
                Justin,Doe
                Jane,Doe
                John,Doe
                """;

        File temp = File.createTempFile("fake-file", ".tmp");
        Files.copy(new ByteArrayInputStream(fakeFile.getBytes()), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        jobLauncher.run(importPersonJob,
                new JobParametersBuilder().addString("inputFilePath", temp.getAbsolutePath()
                ).toJobParameters());
    }
}
