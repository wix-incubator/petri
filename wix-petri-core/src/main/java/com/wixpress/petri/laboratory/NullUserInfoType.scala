package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.Experiment
import com.wixpress.petri.experiments.domain.TestGroup
import scala.Option
import java.util.UUID

class NullUserInfoType extends UserInfoType {

  def isAnonymous: Boolean = true

  def drawTestGroup(exp: Experiment): TestGroup = {
    throw new UnsupportedOperationException("cannot conduct experiments when user info is NullUserInfo - are you trying to use Petri from a non-http flow?")
  }

  def persistentKernel: Option[UUID] = None


}