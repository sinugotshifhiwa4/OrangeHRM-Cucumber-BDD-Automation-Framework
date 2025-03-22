package com.orangehrm.pages.base;

public enum DropdownSelector {

    VISIBLE_TEXT("visibletext"),
    VALUE("value"),
    INDEX("index");

    private final String selector;

    DropdownSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }
}
