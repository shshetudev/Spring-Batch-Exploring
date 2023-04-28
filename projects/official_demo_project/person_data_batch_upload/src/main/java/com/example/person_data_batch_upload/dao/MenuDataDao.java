package com.example.person_data_batch_upload.dao;

import com.example.person_data_batch_upload.model.entity.MenuData;
import lombok.Data;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
@Data
public class MenuDataDao {
    private final JdbcTemplate jdbcTemplate;

    public void insertMenuData(List<MenuData> menuDataList) {
        final String INSERT_MENU_DATA_SQL = """
            INSERT INTO menu_data(menu_text, service_budget_id, shop_id, last_modify_date, menu_images_cloud_data) 
            VALUES (?, ?, ?, ?, ?)""";
        jdbcTemplate.batchUpdate(INSERT_MENU_DATA_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MenuData menuData = menuDataList.get(i);
                ps.setString(1, menuData.getMenuText());
                ps.setInt(2, menuData.getServiceBudgetId());
                ps.setInt(3, menuData.getShopId());
                ps.setLong(4, System.currentTimeMillis());
                ps.setString(5, menuData.getMenuImagesCloudData());
            }

            @Override
            public int getBatchSize() {
                return menuDataList.size();
            }
        });
    }
}
