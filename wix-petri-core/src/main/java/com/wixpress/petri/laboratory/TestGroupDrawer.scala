package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.{Experiment, TestGroup}


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */
@deprecated(message = "use ConductionStrategy instead",since = "5/18/2015")
trait TestGroupDrawer {
  def drawTestGroup(exp: Experiment): TestGroup
}






