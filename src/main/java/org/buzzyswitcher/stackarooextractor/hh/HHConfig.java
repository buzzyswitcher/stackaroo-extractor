package org.buzzyswitcher.stackarooextractor.hh;

import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class HHConfig {

    @Bean
    public AbstractUrlCreator hhUrlCreator() {
        AbstractUrlCreator urlCreator = new HHUrlCreator();
        urlCreator.setScheme("https");
        urlCreator.setHost("api.hh.ru");
        urlCreator.setPath("vacancies");

        return urlCreator;
    }
}
