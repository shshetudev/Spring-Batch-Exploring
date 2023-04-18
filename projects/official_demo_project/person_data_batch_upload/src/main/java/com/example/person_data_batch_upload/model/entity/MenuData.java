package com.example.person_data_batch_upload.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuData {
    private String menuText;
    private Integer serviceBudgetId;
    private Integer shopId;
    private Long lastModifyDate;
    private String menuImagesCloudData;
}
