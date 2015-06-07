package com.wixpress.petri.laboratory

import java.util.UUID

import com.wixpress.petri.experiments.domain.{Experiment, TestGroup}

class AnonymousUserInfoType extends UserInfoType {

  def isAnonymous: Boolean = true

  def drawTestGroup(exp: Experiment): TestGroup = {
    return new AnonymousTestGroupAssignmentStrategy().getAssignment(exp, null)
  }

  def persistentKernel: Option[UUID] = None

  override def shouldPersist: Boolean = true


}