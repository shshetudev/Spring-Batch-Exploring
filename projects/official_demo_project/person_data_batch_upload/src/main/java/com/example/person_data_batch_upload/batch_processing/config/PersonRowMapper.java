package com.example.person_data_batch_upload.batch_processing.config;

import com.example.person_data_batch_upload.model.entity.Person;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class PersonRowMapper implements RowMapper<Person> {
    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person person = new Person();
        person.setFirstName(rs.getString("first_name"));
        person.setLastName(rs.getString("last_name"));
        person.setServiceId(rs.getInt("service_id"));
        person.setShopId(rs.getInt("shop_id"));
        return person;
    }
}
