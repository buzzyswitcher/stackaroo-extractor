package org.buzzyswitcher.stackarooextractor.dao.entity.nsi;

public enum ThemeEnum {

    JAVA("java");

    private final String text;

    ThemeEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
