package org.buzzyswitcher.stackarooextractor.urlconstructor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.hh.HHConfig;
import org.buzzyswitcher.stackarooextractor.hh.HHUrl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlManagerTest {

    HHUrl manager = new UrlManager();

    @Test
    void should_return_valid_HH_vacancies_ULR() {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(HHConfig.TEXT, "java"));
        params.add(new BasicNameValuePair(HHConfig.CURRENT_PAGE, "1"));
        params.add(new BasicNameValuePair(HHConfig.ITEMS_ON_PAGE, "100"));
        String url = manager.getHHVacanciesId(params);
        assertEquals("https://api.hh.ru/vacancies?text=java&page=1&per_page=100", url);
    }

    @Test
    void should_return_valid_HH_vacancy_URL() {
        String url = manager.getHHVacancy("66855883");
        assertEquals("https://api.hh.ru/vacancies/66855883", url);
    }
}