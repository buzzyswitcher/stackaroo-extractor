package org.buzzyswitcher.stackarooextractor.hh;

import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class HHUrlCreatorTestConfig {

    @Bean(name = "HHUrlCreatorTest")
    public AbstractUrlCreator hhUrlCreator() {
        AbstractUrlCreator urlCreator = new HHUrlCreator();
        urlCreator.setScheme("https");
        urlCreator.setHost("api.hh.ru");
        urlCreator.setPath("vacancies");

        return urlCreator;
    }
}
