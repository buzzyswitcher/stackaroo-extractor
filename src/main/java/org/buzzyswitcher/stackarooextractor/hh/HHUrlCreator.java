package org.buzzyswitcher.stackarooextractor.hh;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.buzzyswitcher.stackarooextractor.urlconstructor.AbstractUrlCreator;

import java.util.List;

public class HHUrlCreator extends AbstractUrlCreator {

    protected static final String AREA_ID = "area";
    protected static final String TEXT = "text";
    protected static final String CURRENT_PAGE = "page";
    protected static final String ITEMS_ON_PAGE = "per_page";
    protected static final String DATE_TO = "date_to";
    protected static final String DATE_FROM = "date_from";

    private static final String DATE_PATTERN = "YYYY-MM-DD";

    @Override
    public String build(List<NameValuePair> queryParams) {
        URIBuilder builder = new URIBuilder()
                .setScheme(super.getScheme())
                .setHost(super.getHost())
                .setPath(super.getPath())
                .setParameters(queryParams);

        return builder.toString();
    }
}
