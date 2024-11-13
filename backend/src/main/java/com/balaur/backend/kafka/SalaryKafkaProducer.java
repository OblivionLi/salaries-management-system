package com.balaur.backend.kafka;

import com.balaur.backend.models.Salary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SalaryKafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topic = "salary-topic";
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendSalaryMessage(Salary salary) {
        try {
            salary.setSalaryDate(null); // HACK to bypass the stupid jackson old module related with dates
            String message = mapper.writeValueAsString(salary);
            kafkaTemplate.send(topic, message);
            System.out.println("Sent message: " + message);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            System.err.println("Error serializing salary object.");
        }
    }
}
