package com.wixpress.petri;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RunnableServer {

    private static Main petriServer;

    public static void main(String[] args) throws Exception {
        start();
    }


    public static void start() throws Exception {
        registerDatabaseDriver();
        petriServer = Main.createPetriServer();
        final String databaseUrl = petriServer.getDatabaseUrl();
        initDatabase(databaseUrl);
        petriServer.start();
    }

    public static void stop() throws Exception {
        petriServer.stop();
    }


    private static void registerDatabaseDriver() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    private final static String CREATE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";

    static void initDatabase(String databaseUrl) throws SQLException {
        List<String> queries = Arrays.asList(
                "experiments (   id INT AUTO_INCREMENT,   experiment MEDIUMTEXT,   last_update_date BIGINT,   orig_id INT,   start_date BIGINT DEFAULT 0,   end_date BIGINT DEFAULT 4102444800000,   PRIMARY KEY(id, last_update_date) )",
                "specs (   id INT PRIMARY KEY AUTO_INCREMENT,   fqn VARCHAR (255) NOT NULL,   spec MEDIUMTEXT,   UNIQUE KEY (fqn) )",
                "metricsReport (   server_name VARCHAR (255) NOT NULL,   experiment_id INT NOT NULL,   experiment_value VARCHAR (255) NOT NULL,   total_count BIGINT,   five_minutes_count BIGINT,   last_update_date BIGINT,   PRIMARY KEY (server_name, experiment_id, experiment_value) )",
                "userState (   user_id VARCHAR (50) NOT NULL,   state VARCHAR (4096),   date_updated BIGINT NOT NULL,   PRIMARY KEY(user_id) )"
        );

        try (Connection conn = DriverManager.getConnection(databaseUrl)) {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource(conn, false);
            final JdbcTemplate template = new JdbcTemplate(dataSource);
            for (String query: queries) {
                template.execute(CREATE_IF_NOT_EXISTS + query);
            }
        }
    }
}
