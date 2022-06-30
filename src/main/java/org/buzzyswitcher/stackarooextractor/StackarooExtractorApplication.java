package org.buzzyswitcher.stackarooextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class StackarooExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(StackarooExtractorApplication.class, args);
    }

}
