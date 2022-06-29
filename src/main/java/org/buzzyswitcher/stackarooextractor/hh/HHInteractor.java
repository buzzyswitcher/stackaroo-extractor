package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.model.HHVacanciesIdResponse;
import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HHInteractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HHInteractor.class);

    RestTemplate restTemplate;
    AbstractUrlCreator hhUrlCreator;

    public HHInteractor(RestTemplate restTemplate, AbstractUrlCreator hhUrlCreator) {
        this.restTemplate = restTemplate;
        this.hhUrlCreator = hhUrlCreator;
    }

    public Set<String> getIds() {
        Set<String> ids = new HashSet<>();
        int maxPage = getMaxPagesForQuery();
        LOGGER.info("Max page: [{}}", maxPage);
        int page = 0;
        while (page <= maxPage) {
            List<NameValuePair> queryParams = new ArrayList<>();
            queryParams.add(new BasicNameValuePair(HHUrlCreator.TEXT, "java"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, "2760"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, "100"));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_FROM, LocalDate.now().minusDays(1).toString()));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_TO, LocalDate.now().toString()));
            queryParams.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, String.valueOf(page)));
            String url = hhUrlCreator.build(queryParams);
            LOGGER.info("Current URL: [{}]", url);
            ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
            Set<String> idInPage = response.getBody().getItems().stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toSet());
            ids.addAll(idInPage);
            page++;
        }

        return ids;
    }

    private int getMaxPagesForQuery() {
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair(HHUrlCreator.TEXT, "java"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.AREA_ID, "2760"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.CURRENT_PAGE, "0"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.ITEMS_ON_PAGE, "100"));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_FROM, LocalDate.now().minusDays(1).toString()));
        queryParams.add(new BasicNameValuePair(HHUrlCreator.DATE_TO, LocalDate.now().toString()));

        String url = hhUrlCreator.build(queryParams);
        ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
        return response.getBody().getPages();
    }

}
