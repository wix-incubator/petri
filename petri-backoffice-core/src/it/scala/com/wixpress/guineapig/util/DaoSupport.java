package com.wixpress.guineapig.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;

public abstract class DaoSupport {
    private static ITEmbeddedMysql mysql = new ITEmbeddedMysql(3310);

    @BeforeClass
    public static void startMysql() {
        mysql.start();
    }

    @AfterClass
    public static void stopMysql() {
        mysql.stop();
    }

    public static DataSource getDataSource() {
        return mysql.dataSource();
    }
}
