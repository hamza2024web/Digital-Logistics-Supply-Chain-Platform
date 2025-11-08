package com.spring.digital_logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalLogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalLogisticsApplication.class, args);
    }

}
