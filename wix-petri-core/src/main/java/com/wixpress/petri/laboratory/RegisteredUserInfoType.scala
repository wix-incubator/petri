package com.wixpress.petri.laboratory

import java.util.UUID

import com.wixpress.petri.experiments.domain.{TestGroup, Experiment}

case class RegisteredUserInfoType(userId: UUID) extends UserInfoType {

  def isAnonymous: Boolean = false

  def drawTestGroup(exp: Experiment): TestGroup = ConductionStrategy.drawTestGroupForId(exp, userId.toString)

  def persistentKernel: Option[UUID] = Some(userId)


}