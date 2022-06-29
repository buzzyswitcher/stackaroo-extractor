package org.buzzyswitcher.stackarooextractor.urlconstructor;

import lombok.Data;
import org.apache.http.NameValuePair;

import java.util.List;

@Data
public abstract class AbstractUrlCreator implements UrlCreator {

    private String scheme;
    private String host;
    private String path;

    @Override
    public String build(List<NameValuePair> queryParams) {
        return null;
    }
}
