package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;

import java.util.List;

public interface HHUrl {

    String getHHVacanciesId(List<NameValuePair> queryParams);
    String getHHVacancy(String vacancyId);
}
