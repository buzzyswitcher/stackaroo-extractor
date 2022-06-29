package org.buzzyswitcher.stackarooextractor.urlconstructor;

import org.apache.http.NameValuePair;

import java.util.List;

public interface UrlCreator {

    String build(List<NameValuePair> queryParams);
}
