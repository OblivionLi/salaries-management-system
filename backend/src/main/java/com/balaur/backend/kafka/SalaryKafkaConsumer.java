package com.balaur.backend.kafka;

import com.balaur.backend.models.Salary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SalaryKafkaConsumer {
    private final ObjectMapper mapper = new ObjectMapper();


    @KafkaListener(topics = "salary-topic", groupId = "mygroup_dev")
    public void consumeSalaryMessage(String message) {
        try {
            Salary salary = mapper.readValue(message, Salary.class);
            System.out.println("Received salary message: " + salary);
            // Process salary message here, for example:
            // - send a notification
            // - trigger an event
            // - log the salary processing or error handling
            // - etc..
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error deserializing salary object.");
        }
    }
}
