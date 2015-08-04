package com.wixpress.petri;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 9/9/14
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
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
