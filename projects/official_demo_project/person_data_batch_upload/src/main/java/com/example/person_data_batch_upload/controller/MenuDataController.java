package com.example.person_data_batch_upload.controller;

import com.example.person_data_batch_upload.model.entity.MenuData;
import com.example.person_data_batch_upload.service.MenuDataService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.person_data_batch_upload.constants.MenuDataConstants.GSP_LARGE_VOLUME_MENU_DATA_UPLOAD_API;

@RestController
@RequestMapping("/jobs")
@Slf4j
public class MenuDataController {
    private final JobLauncher jobLauncher;
    private final Job importMenuDataJob;
    private final Job updateOldAndInsertNewMenuDataJob;
    private final MenuDataService menuDataService;

//    private static final String MENU_DATA_INSERT_API = "/data/jp_beauty_storage/menu?sid=jp_beauty_shop_menu_001&authkey=jpbeautyshopmenu001";
    private static final String MENU_DATA_INSERT_API = "/data/jp_beauty_storage/menu";

    public MenuDataController(JobLauncher jobLauncher,
                              @Qualifier("importMenuDataJob") Job importMenuDataJob,
                              @Qualifier("updateMenuDataJob") Job updateOldAndInsertNewMenuDataJob, MenuDataService menuDataService) {
        this.jobLauncher = jobLauncher;
        this.importMenuDataJob = importMenuDataJob;
        this.updateOldAndInsertNewMenuDataJob = updateOldAndInsertNewMenuDataJob;
        this.menuDataService = menuDataService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importMenuData() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "importMenuDataJob")
                .addLong("startsAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importMenuDataJob, jobParameters);
        return ResponseEntity.ok("Import job started");
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateMenuData() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "updateMenuDataJob")
                .addLong("updatedAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(updateOldAndInsertNewMenuDataJob, jobParameters);
        return ResponseEntity.ok("Update job started");
    }

    @PostMapping(MENU_DATA_INSERT_API)
    public ResponseEntity<String> insertMenuData(@RequestBody List<MenuData> menuDataList) {
        log.info("Request body: {}", menuDataList);
        menuDataService.saveMenuData(menuDataList);
        return ResponseEntity.ok("Menu data inserted these menus: " + menuDataList);
    }
}
