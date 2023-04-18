package com.example.person_data_batch_upload.batch_processing;

import com.example.person_data_batch_upload.model.entity.MenuData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MenuDataItemProcessor implements ItemProcessor<MenuData, MenuData> {
    // todo: We have to write business logic/validation logic here
    @Override
    public MenuData process(final MenuData menuData) throws Exception {
        // todo: Add processing logic

        final MenuData transformedMenuData = new MenuData(
                menuData.getMenuText(),
                menuData.getServiceBudgetId(),
                menuData.getShopId(),
                menuData.getLastModifyDate(),
                menuData.getMenuImagesCloudData());
        log.info("Converting (" + menuData + ") into (" + transformedMenuData + ")");
        return transformedMenuData;
    }
}
