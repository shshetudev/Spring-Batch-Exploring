package com.example.person_data_batch_upload.client;

import com.example.person_data_batch_upload.model.entity.MenuData;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.person_data_batch_upload.constants.MenuDataConstants.BASE_URL;
import static com.example.person_data_batch_upload.constants.MenuDataConstants.GSP_LARGE_VOLUME_MENU_DATA_UPLOAD_API;

// todo: change the host and port
// ${my.api.url}

@FeignClient(
        name = "gsp-menu-data-client",
        url = BASE_URL
)
public interface MenuDataClient {

    @Headers("Content-Type: application/json")
    @PostMapping(value = GSP_LARGE_VOLUME_MENU_DATA_UPLOAD_API)
    void saveMenuData(@RequestBody List<MenuData> menuDataList);
}
