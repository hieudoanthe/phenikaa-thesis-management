package com.phenikaa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class CommunicationServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CommunicationServiceApplication.class, args);
    }
}
