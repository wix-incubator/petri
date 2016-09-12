package com.wixpress.guineapig.entities.ui;

/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * Date: 3/18/14
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public enum MainFilter {
    OPEN_TO_ALL("Open To All"), WIX_USERS_ONLY("Wix Users Only"), FILTERS("Filters");

    private String type;

    private MainFilter(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }
}
