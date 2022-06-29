package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.framework.qual.QualifierArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HHUrlCreatorTestConfig.class})
class HHUrlCreatorTest {

    @Autowired
    @Qualifier("HHUrlCreatorTest")
    AbstractUrlCreator hhUrlCreator;

    @Test
    void should_create_valid_url() {
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair(HHUrlCreator.TEXT, "java"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, "1"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, "0"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, "100"));
        Assertions.assertEquals("https://api.hh.ru/vacancies?text=java&area=1&page=0&per_page=100", hhUrlCreator.build(queryParams));
    }


}