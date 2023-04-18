package com.example.person_data_batch_upload.batch_processing;

import com.example.person_data_batch_upload.model.entity.MenuData;
import com.example.person_data_batch_upload.model.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShopDataItemProcessor implements ItemProcessor<Shop, Shop> {
    // todo: We have to write business logic/validation logic here
    @Override
    public Shop process(final Shop shop) throws Exception {
        // todo: Add processing logic

        final Shop transformedShopData = new Shop(shop.getShopId());
        log.info("Converting (" + shop + ") into (" + transformedShopData + ")");
        return transformedShopData;
    }
}
