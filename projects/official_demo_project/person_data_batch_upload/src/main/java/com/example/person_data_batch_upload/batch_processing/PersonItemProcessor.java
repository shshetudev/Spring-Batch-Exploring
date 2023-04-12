package com.example.person_data_batch_upload.batch_processing;

import com.example.person_data_batch_upload.model.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PersonItemProcessor implements ItemProcessor<Person, Person> {
    // todo: We have to write business logic/validation logic here
    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();
        final Integer serviceId = person.getServiceId();
        final Integer shopId = person.getShopId();

        final Person transformedPerson = new Person(firstName, lastName, serviceId, shopId);
        log.info("Converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }
}
