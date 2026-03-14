package ru.kpfu.itis.efremov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SchemaRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchemaRiskApplication.class, args);
    }
}
