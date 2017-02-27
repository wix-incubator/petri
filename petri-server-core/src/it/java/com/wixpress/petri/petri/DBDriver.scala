package com.wixpress.petri.petri

import java.sql.{Connection, DriverManager, ResultSet, SQLException}

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.wixpress.petri.experiments.domain.ExperimentSnapshot
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import com.wixpress.petri.petri.DBDriver.{ExperimentsRow, ExperimentsRowMapper}
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper, RowMapperResultSetExtractor}
import org.springframework.jdbc.datasource.SingleConnectionDataSource

/**
 * @author talyag
 * @since 9/15/13
 */
class DBDriver(val jdbcTemplate: JdbcTemplate, objectMapper: ObjectMapper) {

  def createReadOnlyH2User() = {
    jdbcTemplate.execute("CREATE USER IF NOT EXISTS auser_ro PASSWORD \'as\'")
    jdbcTemplate.execute("GRANT SELECT ON experiments to auser_ro")
  }

  def createReadOnlyMysqlUser(url: String) = {
    val conn: Connection = DriverManager.getConnection(url, "root", null)
    val dataSource: SingleConnectionDataSource = new SingleConnectionDataSource(conn, false)
    val rootTemplate = new JdbcTemplate(dataSource)

    rootTemplate.execute("GRANT SELECT ON *.* TO \'auser_ro\'@\'%%\' IDENTIFIED BY \'as\'")
    rootTemplate.execute("FLUSH PRIVILEGES")
  }

  def createSchema() {
    dropTables()

    // 4102444800000 millis == 2100-01-01T00:00:00
    jdbcTemplate.execute(
      """CREATE TABLE experiments (
        |  id INT AUTO_INCREMENT,
        |  experiment MEDIUMTEXT,
        |  last_update_date BIGINT,
        |  orig_id INT,
        |  start_date BIGINT DEFAULT 0,
        |  end_date BIGINT DEFAULT 4102444800000,
        |  PRIMARY KEY(id, last_update_date)
        |)""".stripMargin)

    jdbcTemplate.execute(
      """CREATE TABLE specs (
        |  id INT PRIMARY KEY AUTO_INCREMENT,
        |  fqn VARCHAR (255) NOT NULL,
        |  spec MEDIUMTEXT,
        |  UNIQUE KEY (fqn)
        |)""".stripMargin)

    jdbcTemplate.execute(
      """CREATE TABLE metricsReport (
        |  server_name VARCHAR (255) NOT NULL,
        |  experiment_id INT NOT NULL,
        |  experiment_value VARCHAR (255) NOT NULL,
        |  total_count BIGINT,
        |  five_minutes_count BIGINT,
        |  last_update_date BIGINT,
        |  PRIMARY KEY (server_name, experiment_id, experiment_value)
        |)""".stripMargin)

    jdbcTemplate.execute(
      """CREATE TABLE userState (
        |  user_id VARCHAR (50) NOT NULL,
        |  state VARCHAR (4096),
        |  date_updated BIGINT NOT NULL,
        |  PRIMARY KEY(user_id)
        |)""".stripMargin)
  }

  def dropTables() {
    jdbcTemplate.execute("DROP TABLE IF EXISTS experiments")
    jdbcTemplate.execute("DROP TABLE IF EXISTS specs")
    jdbcTemplate.execute("DROP TABLE IF EXISTS metricsReport")
    jdbcTemplate.execute("DROP TABLE IF EXISTS userState")
  }

  def emptyTables() {
    jdbcTemplate.execute("TRUNCATE TABLE experiments")
    jdbcTemplate.execute("TRUNCATE TABLE specs")
    jdbcTemplate.execute("TRUNCATE TABLE metricsReport")
    jdbcTemplate.execute("TRUNCATE TABLE userState")
  }

  def insertIllegalExperiment() {
    jdbcTemplate.update("insert into experiments(id,last_update_date,experiment) values (1,0,'illegalExperiment')")
  }

  @throws(classOf[JsonProcessingException])
  def insertExperiment(experiment: ExperimentSnapshot) {
    jdbcTemplate.update("INSERT INTO experiments (id, last_update_date, experiment) values (?, ?, ?)",
      experiment.originalId.asInstanceOf[Object], experiment.creationDate.getMillis.asInstanceOf[Object], objectMapper.writeValueAsString(experiment))
  }

  def getExperimentStartEndDates(origId: Int): Seq[Interval] = {
    import scala.collection.JavaConversions.asScalaBuffer

    jdbcTemplate.query(
      "SELECT id, experiment, last_update_date, orig_id, start_date, end_date FROM experiments WHERE orig_id = ? ORDER BY id ASC",
      new ExperimentsRowMapper,
      Int.box(origId)
    ).map(e => new Interval(new DateTime(e.startDate, DateTimeZone.UTC), new DateTime(e.endDate, DateTimeZone.UTC)))
  }

  def getExperimentRows: Seq[ExperimentsRow] = {
    import scala.collection.JavaConversions.asScalaBuffer
    jdbcTemplate.query(
      "SELECT id, experiment, last_update_date, orig_id, start_date, end_date FROM experiments ORDER BY id ASC, last_update_date ASC",
      new RowMapperResultSetExtractor(new ExperimentsRowMapper)
    )
  }

  def getExperimentCountByOrigId(origId: Int): Int = {
    jdbcTemplate.queryForObject("SELECT count(1) FROM experiments WHERE orig_id = ?", classOf[Integer],
      Int.box(origId))
  }

  def getExperimentCountByIdAndOrigId(id: Int, origId: Int): Int = {
    jdbcTemplate.queryForObject("SELECT count(1) FROM experiments WHERE id = ? AND orig_id = ?", classOf[Integer],
      Int.box(id), Int.box(origId))
  }

  def insertSpec(serializedSpec: String, key: String) {
    jdbcTemplate.update("INSERT INTO specs(fqn, spec) values (?, ?)", key, serializedSpec)
  }

  import DBDriver.createTemplateRO
  def getJdbcTemplateRO(url: String): JdbcTemplate = {
    createTemplateRO(url)
  }

  @throws(classOf[SQLException])
  def closeConnection() {
    jdbcTemplate.getDataSource.getConnection.close()
  }
}

object DBDriver {

  val JDBC_H2_IN_MEM_CONNECTION_STRING = "jdbc:h2:mem:test;MODE=MySQL"

  @throws(classOf[ClassNotFoundException])
  @throws(classOf[SQLException])
  def dbDriver(url: String): DBDriver = {
    new DBDriver(createTemplate(url), ObjectMapperFactory.makeObjectMapper)
  }

  private def createTemplate(url: String): JdbcTemplate = {
    val conn: Connection = DriverManager.getConnection(url, "auser", "sa")
    val dataSource: SingleConnectionDataSource = new SingleConnectionDataSource(conn, false)
    new JdbcTemplate(dataSource)
  }

  private def createTemplateRO(url: String): JdbcTemplate = {
    val conn: Connection = DriverManager.getConnection(url, "auser_ro", "as")
    val dataSource: SingleConnectionDataSource = new SingleConnectionDataSource(conn, false)
    new JdbcTemplate(dataSource)
  }

  case class ExperimentsRow(id: Int,
                            experiment: String,
                            lastUpdateDate: Long,
                            origId: Int,
                            startDate: Long,
                            endDate: Long)

  class ExperimentsRowMapper extends RowMapper[ExperimentsRow] {
    override def mapRow(rs: ResultSet, rowNum: Int): ExperimentsRow = {
      ExperimentsRow(rs.getInt(1), rs.getString(2), rs.getLong(3), rs.getInt(4), rs.getLong(5), rs.getLong(6))
    }
  }
}
