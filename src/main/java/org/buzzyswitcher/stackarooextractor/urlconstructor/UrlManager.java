package org.buzzyswitcher.stackarooextractor.urlconstructor;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.buzzyswitcher.stackarooextractor.hh.HHConfig;
import org.buzzyswitcher.stackarooextractor.hh.HHUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.List;

@Service
public class UrlManager implements HHUrl {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlManager.class);

    @Override
    public String getHHVacanciesId(List<NameValuePair> queryParams) {
        return getUrlOnlyWithParams(queryParams);
    }

    @Override
    public String getHHVacancy(String vacancyId) {
        String url = null;
        try {
            url = new URIBuilder().setScheme(HHConfig.SCHEME)
                    .setHost(HHConfig.HOST)
                    .setPathSegments(HHConfig.PATH, vacancyId)
                    .build().toString();
        } catch (URISyntaxException e) {
            LOGGER.warn("Problems with URI syntax: [{}] with reason [{}]", e.getMessage(), e.getReason());
        }
        return url;
    }

    private String getUrlOnlyWithParams(List<NameValuePair> queryParams) {
        String url = null;
        try {
            url = new URIBuilder().setScheme(HHConfig.SCHEME)
                    .setHost(HHConfig.HOST)
                    .setPath(HHConfig.PATH)
                    .setParameters(queryParams)
                    .build().toString();
        } catch (URISyntaxException e) {
            LOGGER.warn("Problems with URI syntax: [{}] with reason [{}]", e.getMessage(), e.getReason());
        }
        return url;
    }
}
