package com.example.person_data_batch_upload.batch_processing.row_mapper;

import com.example.person_data_batch_upload.model.entity.MenuData;
import com.example.person_data_batch_upload.model.entity.Shop;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class ShopDataRowMapper implements RowMapper<Shop> {
    @Override
    public Shop mapRow(ResultSet rs, int rowNum) throws SQLException {
        Shop shop = new Shop();
        shop.setShopId(rs.getInt("shop_id"));
        return shop;
    }
}
