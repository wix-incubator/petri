package com.wixpress.guineapig.entities.ui

import java.{util => ju}

import com.wixpress.petri.petri.ConductExperimentSummary
import org.joda.time.DateTime

import scala.collection.JavaConversions._

object ExperimentReportBuilder {

  def buildReport(experimentId: Int, summaries: ju.List[ConductExperimentSummary]): ExperimentReport = {
    if (summaries.isEmpty)
      emptyExperimentReport(experimentId)
    else
      buildExperimentReport(experimentId, summaries.map(countFiveMinutesOnlyIfRecentEnough))
  }

  private def countFiveMinutesOnlyIfRecentEnough(value: ConductExperimentSummary): ConductExperimentSummary = {
    if (value.lastUpdated.isAfter(new DateTime().minusMinutes(5))) value else value.copy(fiveMinuteCount = 0)
  }

  private def emptyExperimentReport(experimentId: Int): ExperimentReport = {
    ExperimentReport(experimentId, 0, 0, new DateTime(0), Nil)
  }

  private def buildExperimentReport(experimentId: Int,
                                    summariesForDisplay: Seq[ConductExperimentSummary]): ExperimentReport = {
    ExperimentReport(
      experimentId,
      getTotalFiveMinCount(summariesForDisplay),
      getTotalCount(summariesForDisplay),
      getLatestOf(summariesForDisplay),
      groupByValue(summariesForDisplay)
    )
  }

  private def getLatestOf(summaries: Seq[ConductExperimentSummary]): DateTime = {
    summaries.maxBy(_.lastUpdated.getMillis).lastUpdated
  }

  private def groupByValue(summariesForDisplay: Seq[ConductExperimentSummary]): Seq[PerValueReport] = {
    summariesForDisplay
      .groupBy(_.experimentValue)
      .map { case (value, summariesByValue) =>
        buildPerValueReport(value, summariesByValue)
      }.toSeq
  }

  private def buildPerValueReport(value: String, summariesByValue: Seq[ConductExperimentSummary]): PerValueReport = {
    PerValueReport(
      value,
      getTotalFiveMinCount(summariesByValue),
      getTotalCount(summariesByValue),
      getLatestOf(summariesByValue),
      groupByServer(summariesByValue)
    )
  }

  private def getTotalCount(summaries: Seq[ConductExperimentSummary]): Long = {
    summaries.map(_.totalCount).sum
  }

  private def getTotalFiveMinCount(summaries: Seq[ConductExperimentSummary]): Long = {
    summaries.map(_.fiveMinuteCount).sum
  }

  private def groupByServer(summariesByValue: Seq[ConductExperimentSummary]): Seq[PerServerReport] = {
    summariesByValue.groupBy(_.serverName).map { case (server, summariesByServer) =>
      buildPerServerReport(server, summariesByServer)
    }.toSeq
  }

  private def buildPerServerReport(server: String,
                                   summariesByServer: Seq[ConductExperimentSummary]): PerServerReport = {
    PerServerReport(
      server, getTotalFiveMinCount(summariesByServer), getTotalCount(summariesByServer), getLatestOf(summariesByServer)
    )
  }
}
