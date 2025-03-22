package com.orangehrm.utils.constants;

public class GlobalConstants {

    private static final String CHROME = "CHROME_BROWSER";
    private  static final String FIREFOX = "FIREFOX_BROWSER";
    private  static final String EDGE = "EDGE_BROWSER";
    private static final String PORTAL_BASE_URL = "PORTAL_BASE_URL";

    public static String getChromeBrowser() {
        return CHROME;
    }

    public static String getFirefoxBrowser() {
        return FIREFOX;
    }

    public static String getEdgeBrowser() {
        return EDGE;
    }

    public static String getPortalBaseUrl() {
        return PORTAL_BASE_URL;
    }
}
