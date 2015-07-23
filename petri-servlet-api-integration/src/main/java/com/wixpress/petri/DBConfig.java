package com.wixpress.petri;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class DBConfig {
    public final String username;
    public final String password;
    public final String url;

    private DBConfig(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public static DBConfig makeDBConfig(String username, String password, String url) {
        return new DBConfig(username, password, url);
    }
}
