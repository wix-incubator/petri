package com.wixpress.guineapig.topology;

/**
 * @author dalias
 * @since 9/28/14
 */
public class GuineapigDBTopology {
    public String username;
    public String password;
    public String url;
    public String driverClassName = "com.mysql.jdbc.Driver";
    public int minPoolSize = 2;
    public int initialPoolSize = minPoolSize;
    public int maxPoolSize = 50;
    public int acquireIncrement = 2;
    public boolean testConnectionOnCheckin = true;
    public boolean testConnectionOnCheckout = false;
    public String preferredTestQuery = "SELECT 1;";
    public boolean autoCommitOnClose = true;
    public int idleConnectionTestPeriod = 60;
    public int maxIdleTime = 60 * 60; // 1 hour
    public int maxIdleTimeExcessConnections = 120;
    public int checkoutTimeout = 1000;
    public int numHelperThreads = 6;
    public int unreturnedConnectionTimeout = 0;
    public boolean debugUnreturnedConnectionStackTraces = false;
    public int queryTimeout = 0;

}
