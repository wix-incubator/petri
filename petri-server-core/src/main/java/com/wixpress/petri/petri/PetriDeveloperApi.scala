package com.wixpress.petri.petri

import java.util
import java.util.UUID

import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSpec}

/**
 * @author Dalias
 * @since 3/22/15
 */
trait PetriDeveloperApi {

  def getFullUserState(userGuid: UUID): UserState

  def migrateStartEndDates(): Unit

  def addSpecNoValidation(spec :ExperimentSpec): Unit

  def fetchExperimentsGoingToEndDueToDate(minutesEnded: Int): util.List[Experiment]

}
