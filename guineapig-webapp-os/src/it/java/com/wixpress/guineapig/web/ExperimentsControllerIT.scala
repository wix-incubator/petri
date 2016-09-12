package com.wixpress.guineapig.web

import java.util.{Arrays, List => JList}

import com.wixpress.guineapig.drivers.SpecificationWithEnvSupport
import com.wixpress.guineapig.entities.ui.{UiExperiment, UiExperimentBuilder, UiTestGroup}
import org.specs2.specification.Scope

class ExperimentsControllerIT extends SpecificationWithEnvSupport {

  trait Context extends Scope {

    def createExperiment(groups: JList[UiTestGroup] = Arrays.asList(new UiTestGroup(1, "old", 0), new UiTestGroup(2, "new", 100))): UiExperiment = {
        UiExperimentBuilder
          .anUiExperiment()
          .withid(1)
          .withSpecKey(false)
          .withScope("publicUrl")
          .withGroups(groups)
          .build()
    }
  }

  // todo guineapig-os: test cases are copied from com.wixpress.guineapig.PetriWebappIT

  "Experiment controller" should {
    "create experiment without spec" in new Context {

      val newExperimentResponse = httpDriver.post("http://localhost:9901/v1/Experiments", createExperiment())
      newExperimentResponse.getSuccess must beTrue

      val getExperimentResponse = httpDriver.get(s"http://localhost:9901/v1/Experiment/1")
      getExperimentResponse.getSuccess must beTrue
    }

    "update an existing experiment" in pending

    "terminate an existing experiment" in pending

    "pause an experiment" in pending

    "resume an experiment" in pending

    "expand an experiment" in pending

    "get an experiment reports" in pending

    "add spec exposureId" in pending

    "support experiment conduction limit" in pending

    "not suggest deletion when an experiment with no spec is terminated" in pending
  }
}
