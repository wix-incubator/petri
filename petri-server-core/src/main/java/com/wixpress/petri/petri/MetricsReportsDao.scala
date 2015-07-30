package com.wixpress.petri.petri

import java.sql.ResultSet
import java.{util => ju}

import org.joda.time.DateTime
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}

import scala.collection.JavaConversions._

/**
 * User: Dalias
 * Date: 12/10/14
 * Time: 4:30 PM
 */
trait MetricsReportsDao {
  def getReport(experimentId: Int): ju.List[ConductExperimentSummary]

  def addReports(conductExperimentReports: ju.List[ConductExperimentReport])

  def getReportedExperimentsSince(since: Long): List[TotalExperimentConduction]

  val metricsReportsMapper = new MetricsReportsMapper
  val experimentConductionMapper = new ExperimentConductionMapper
}

class JdbcMetricsReportsDao(jdbcTemplate: JdbcTemplate, fetchReportsDelta: Long) extends MetricsReportsDao {

  private val addReportQuery = "INSERT INTO metricsReport (server_name, experiment_id, experiment_value, total_count, five_minutes_count, last_update_date)  VALUES (?,?,?,?,?,?) "
  private val updateReportQuery = "UPDATE metricsReport SET total_count = ? , five_minutes_count = ? , last_update_date = ? where server_name = ? AND experiment_id = ?  AND experiment_value = ?"
  private val getReportByExperimentID = "SELECT * FROM metricsReport WHERE experiment_id = ? "
  private val getReportByPrimaryKey = "SELECT * FROM metricsReport WHERE server_name = ? AND experiment_id = ?  AND experiment_value = ?"
  private def getLastUpdatedReports(since: Long) =  s"SELECT experiment_id,SUM(total_count) FROM metricsReport where last_update_date > $since GROUP BY experiment_id"

  override def addReports(conductExperimentReports: ju.List[ConductExperimentReport]) {
    conductExperimentReports.foreach(report => {
      getUniqueReport(report) match {
        case None => jdbcTemplate.update(addReportQuery, report.serverName, asInt(report.experimentId) , report.experimentValue, report.count, report.count, asLong((new DateTime).getMillis))
        case Some(result) => jdbcTemplate.update(updateReportQuery, asLong(result.totalCount + report.count), report.count, asLong((new DateTime).getMillis), report.serverName, asInt(report.experimentId), report.experimentValue)
      }

    })
  }

  override def getReport(experimentId: Int): ju.List[ConductExperimentSummary] =
     jdbcTemplate.query(getReportByExperimentID, metricsReportsMapper, asInt(experimentId))


  private def getUniqueReport(conductExperimentReport: ConductExperimentReport): Option[ConductExperimentSummary] =
    try{
      Some(jdbcTemplate.queryForObject(getReportByPrimaryKey, metricsReportsMapper, conductExperimentReport.serverName, asInt(conductExperimentReport.experimentId), conductExperimentReport.experimentValue))
    } catch {
      case ex: IncorrectResultSizeDataAccessException => None
      case ex: Throwable => throw ex
    }

  private def asLong(num : Long) : java.lang.Long = num
  private def asInt(num : Int) : java.lang.Integer = num

  override def getReportedExperimentsSince(scheduledInterval: Long): List[TotalExperimentConduction] = {
    jdbcTemplate.query(getLastUpdatedReports((new DateTime).getMillis - scheduledInterval - fetchReportsDelta), experimentConductionMapper).toList
  }

}

class ExperimentConductionMapper extends RowMapper[TotalExperimentConduction] {
  override def mapRow(rs: ResultSet, rowNum: Int): TotalExperimentConduction = {
    new TotalExperimentConduction(rs.getInt("experiment_id"), rs.getLong("SUM(total_count)"))
  }
}

class MetricsReportsMapper extends RowMapper[ConductExperimentSummary] {
  override def mapRow(rs: ResultSet, rowNum: Int): ConductExperimentSummary = new ConductExperimentSummary(
    rs.getString("server_name"), rs.getInt("experiment_id"), rs.getString("experiment_value"), rs.getLong("five_minutes_count"), rs.getLong("total_count"), new DateTime(rs.getLong("last_update_date")))
}
