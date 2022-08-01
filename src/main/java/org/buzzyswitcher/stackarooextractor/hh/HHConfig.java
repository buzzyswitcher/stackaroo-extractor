package org.buzzyswitcher.stackarooextractor.hh;

import org.springframework.context.annotation.Configuration;

@Configuration
public class HHConfig {

    public static final String SCHEME = "https";
    public static final String HOST = "api.hh.ru";
    public static final String PATH = "vacancies";

    //query params
    public static final String AREA_ID = "area";
    public static final String TEXT = "text";
    public static final String CURRENT_PAGE = "page";
    public static final String ITEMS_ON_PAGE = "per_page";
    public static final String DATE_TO = "date_to";
    public static final String DATE_FROM = "date_from";
    public static final String PROFESSIONAL_ROLE_ID = "professional_role";

    public static final String DATE_PATTERN = "YYYY-MM-DD";

}
