package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.{TestGroup, Experiment}


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */
trait TestGroupDrawer {
  def drawTestGroup(exp: Experiment): TestGroup
}



object RandomTestGroupDrawer extends TestGroupDrawer {
  def drawTestGroup(exp: Experiment): TestGroup = {
    new AnonymousTestGroupAssignmentStrategy().getAssignment(exp, null)
  }
}
