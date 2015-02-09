package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
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
    public final ObjectMapper objectMapper;

    private DBDriver(JdbcTemplate template, ObjectMapper objectMapper) {
        this.jdbcTemplate = template;
        this.objectMapper = objectMapper;
    }

    public static DBDriver dbDriver(String url) throws SQLException, ClassNotFoundException {
        return new DBDriver(createTemplate(url), ObjectMapperFactory.makeObjectMapper());
    }

    private static JdbcTemplate createTemplate(String url) throws ClassNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection(url, "auser", "sa");
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(conn, false);

        return new JdbcTemplate(dataSource);
    }

    public void createMetricsTableSchema(){
        dropMetricsTable();
        jdbcTemplate.execute("CREATE TABLE metricsReport (server_name VARCHAR (255) NOT NULL, experiment_id INT NOT NULL, experiment_value VARCHAR (255) NOT NULL, total_count BIGINT,  five_minutes_count BIGINT , last_update_date BIGINT,  PRIMARY KEY (server_name, experiment_id, experiment_value))");
    }

    public void dropMetricsTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS metricsReport");
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

    public void insertExperiment(ExperimentSnapshot experiment) throws JsonProcessingException {
        jdbcTemplate.update("insert into experiments(id,last_update_date,experiment) values (?,?,?)",
                experiment.originalId(),
                experiment.creationDate().getMillis(),
                objectMapper.writeValueAsString(experiment));
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