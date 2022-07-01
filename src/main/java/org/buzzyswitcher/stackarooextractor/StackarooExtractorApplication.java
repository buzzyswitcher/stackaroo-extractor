package org.buzzyswitcher.stackarooextractor;

import org.buzzyswitcher.stackarooextractor.hh.HHInteractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Set;

@SpringBootApplication
public class StackarooExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(StackarooExtractorApplication.class, args);
    }

}
