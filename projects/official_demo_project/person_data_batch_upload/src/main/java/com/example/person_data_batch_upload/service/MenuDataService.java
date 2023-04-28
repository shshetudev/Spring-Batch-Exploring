package com.example.person_data_batch_upload.service;

import com.example.person_data_batch_upload.dao.MenuDataDao;
import com.example.person_data_batch_upload.model.entity.MenuData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@Slf4j
public class MenuDataService {
    private final MenuDataDao menuDataDao;;

    public void saveMenuData(List<MenuData> menuDataList) {
        log.info("Received menu data list in service layer: {}", menuDataList);
        menuDataDao.insertMenuData(menuDataList);
    }
}
