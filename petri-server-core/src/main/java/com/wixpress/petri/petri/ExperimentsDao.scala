package com.wixpress.petri.petri

import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot}
import org.joda.time.DateTime

/**
 * @author dmitryk
 * @since 17-Sep-2015
 */
trait ExperimentsDao {

  def fetch(): Seq[Experiment]

  def add(experimentSpec: ExperimentSnapshot): Experiment

  def getHistoryById(id: Int): Seq[Experiment]

  def update(experiment: Experiment, currentDateTime: DateTime): Unit

  def fetchByLastUpdate(from: DateTime, to: DateTime): Seq[Experiment]

  def fetchBetweenStartEndDates(now: DateTime): Seq[Experiment]

  def fetchAllExperimentsGroupedByOriginalId: Seq[Experiment]

  def migrateStartEndDates(): Int

  def fetchExperimentById(experimentId: Int) : Option[Experiment]
}
