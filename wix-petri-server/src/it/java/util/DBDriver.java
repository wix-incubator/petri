package util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public class DBDriver {

    public final JdbcTemplate jdbcTemplate;

    private DBDriver(JdbcTemplate template) {
        this.jdbcTemplate = template;
    }

    public static DBDriver dbDriver(String url) throws SQLException, ClassNotFoundException {
        return new DBDriver(createTemplate(url));
    }

    private static JdbcTemplate createTemplate(String url) throws ClassNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection(url, "auser", "sa");
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(conn, false);

        return new JdbcTemplate(dataSource);
    }

    public void createSchema() {
        dropTables();

        jdbcTemplate.execute("CREATE TABLE experiments (id INT AUTO_INCREMENT, experiment MEDIUMTEXT, last_update_date BIGINT, orig_id INT, PRIMARY KEY(id, last_update_date))");
        jdbcTemplate.execute("CREATE TABLE specs (id INT PRIMARY KEY AUTO_INCREMENT, fqn VARCHAR (255) NOT NULL, spec MEDIUMTEXT, UNIQUE KEY (fqn))");
    }

    public void dropTables() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS experiments");
        jdbcTemplate.execute("DROP TABLE IF EXISTS specs");
    }


    public void insertIllegalExperiment() {
        jdbcTemplate.update("insert into experiments(id,last_update_date,experiment) values (1,0,'illegalExperiment')");
    }

    public void insertSpec(final String serializedSpec, String key) {
        jdbcTemplate.update("insert into specs(fqn,spec) values ('" +
                key +
                "','" + serializedSpec + "')");
    }

    public void closeConnection() throws SQLException {
        jdbcTemplate.getDataSource().getConnection().close();
    }
}