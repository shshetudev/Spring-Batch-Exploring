package com.example.person_data_batch_upload.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    private String lastName;
    private String firstName;
    private Integer serviceId;
    private Integer shopId;
}
