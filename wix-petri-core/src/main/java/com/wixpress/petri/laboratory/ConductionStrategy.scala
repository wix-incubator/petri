package com.wixpress.petri.laboratory

import java.util.UUID

import com.wixpress.petri.experiments.domain.{TestGroup, Experiment}

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
trait ConductionStrategy {

  /**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
  def persistentKernel(): Option[UUID]
  def shouldPersist(): Boolean = persistentKernel().isDefined
  def getUserIdRepresentedForFlow(userInSession: Option[UUID]): Option[UUID] = persistentKernel()
  def drawTestGroup(exp: Experiment): TestGroup
}

object ConductionStrategy {

  def drawTestGroupForId(experiment: Experiment, kernel: String) =
    new GuidTestGroupAssignmentStrategy().getAssignment(experiment, kernel)
}

