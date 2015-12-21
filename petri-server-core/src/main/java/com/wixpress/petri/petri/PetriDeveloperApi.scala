package com.wixpress.petri.petri

import java.util.UUID

import com.wixpress.petri.experiments.domain.ExperimentSpec

/**
 * @author Dalias
 * @since 3/22/15
 */
trait PetriDeveloperApi {

  def getFullUserState(userGuid: UUID): UserState

  def migrateStartEndDates(): Unit

  def addSpecNoValidation(spec :ExperimentSpec): Unit

}
