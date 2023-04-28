package com.example.person_data_batch_upload.batch_processing.row_mapper;

import com.example.person_data_batch_upload.model.entity.MenuData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class MenuDataRowMapper implements RowMapper<MenuData> {
    @Override
    public MenuData mapRow(ResultSet rs, int rowNum) throws SQLException {
        MenuData menuData = new MenuData();
        menuData.setMenuText(rs.getString("menu_text"));
        menuData.setServiceBudgetId(rs.getInt("service_budget_id"));
        menuData.setShopId(rs.getInt("shop_id"));
        menuData.setLastModifyDate(System.currentTimeMillis());
        menuData.setMenuImagesCloudData(rs.getString("menu_images_cloud_data"));
        return menuData;
    }
}
