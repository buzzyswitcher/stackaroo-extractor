package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.buzzyswitcher.stackarooextractor.dao.entity.nsi.ThemeEnum;
import org.buzzyswitcher.stackarooextractor.model.HHVacanciesIdResponse;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyId;
import org.buzzyswitcher.stackarooextractor.model.HHVacancyResponse;
import org.buzzyswitcher.stackarooextractor.urlconstructor.UrlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class HHController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HHController.class);

    HHUrl hhUrl;
    HHInteractor interactor;
    RestTemplate restTemplate;

    public HHController(
            UrlManager urlManager,
            RestTemplate restTemplate,
            HHInteractor interactor) {
        this.hhUrl = urlManager;
        this.restTemplate = restTemplate;
        this.interactor = interactor;
    }

    @GetMapping("/hh")
    @Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    public Set<String> test() {
        for (ThemeEnum theme : ThemeEnum.values()) {
            Set<String> unfilteredIds = getIds(theme);
            Set<String> filteredIds = interactor.filterVacancyIds(unfilteredIds, theme);
            Set<HHVacancyResponse> responses = getResponses(filteredIds);
            interactor.convert(responses);
        }

        return Collections.emptySet();
    }

    public Set<String> getIds(ThemeEnum theme) {
        Set<String> ids = new HashSet<>();
        int page = 0;
        int pages = 19;
        while (page <= pages - 1) {
            List<NameValuePair> param = buildParamsForIdQuery(theme.getText(), page, "100", "96", "113");

            String url = hhUrl.getHHVacanciesId(param);
            try {
                url = URLDecoder.decode(url.toString(), "UTF-8"); // java.net class
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LOGGER.info("Current URL: [{}], page: [{}], pages: [{}]", url, page, pages);
            ResponseEntity<HHVacanciesIdResponse> response = restTemplate.getForEntity(url, HHVacanciesIdResponse.class);
            Set<String> idInPage = response.getBody().getItems().stream()
                    .map(HHVacancyId::getId)
                    .collect(Collectors.toSet());
            ids.addAll(idInPage);
            page++;

            if (pages > response.getBody().getPages()) {
                pages = response.getBody().getPages();
            }
        }
        LOGGER.info("ID's: [{}]", ids.toString());

        return ids;
    }

    public Set<HHVacancyResponse> getResponses(Set<String> filteredIds) {
        Set<HHVacancyResponse> responses = new HashSet<>();
        for(String id : filteredIds) {
            String url = hhUrl.getHHVacancy(id);
            HHVacancyResponse response = restTemplate.getForEntity(url, HHVacancyResponse.class).getBody();
            responses.add(response);
        }
        return responses;
    }

    private static List<NameValuePair> buildParamsForIdQuery(String text, int currentPage, String itemsOnPage, String professionalRole, String area) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(HHConfig.TEXT, text));
        params.add(new BasicNameValuePair(HHConfig.AREA_ID, area));
        params.add(new BasicNameValuePair(HHConfig.CURRENT_PAGE, String.valueOf(currentPage)));
        params.add(new BasicNameValuePair(HHConfig.ITEMS_ON_PAGE, itemsOnPage));
        params.add(new BasicNameValuePair(HHConfig.PROFESSIONAL_ROLE_ID, professionalRole));
        return params;
    }
}
