package com.balaur.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;


@Slf4j
@SpringBootApplication
public class BackendApplication {
//    private static Environment env;
//
//    @Autowired
//    public void setEnv(Environment env) {
//        BackendApplication.env = env;
//    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

//        log.info("Datasource URL: {}", env.getProperty("spring.datasource.url"));
//        log.info("Datasource Username: {}", env.getProperty("spring.datasource.username"));
//        log.info("Datasource Password: {}", env.getProperty("spring.datasource.password"));
    }

}
