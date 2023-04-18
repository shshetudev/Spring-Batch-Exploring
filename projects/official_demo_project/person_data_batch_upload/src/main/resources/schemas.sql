DROP TABLE IF EXISTS menuData;

CREATE TABLE menu_data  (
                         menu_id SERIAL,
                         menu_text VARCHAR(20),
                         service_budget_id INTEGER,
                         shop_id INTEGER,
                         last_modify_date bigint,
                         menu_images_cloud_data VARCHAR(50)
);