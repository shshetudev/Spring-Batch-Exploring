DROP TABLE IF EXISTS person;

CREATE TABLE person  (
                         person_id SERIAL,
                         first_name VARCHAR(20),
                         last_name VARCHAR(20),
                         service_id INTEGER,
                         shop_id INTEGER
);