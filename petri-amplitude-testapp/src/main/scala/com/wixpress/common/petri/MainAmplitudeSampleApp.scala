package com.wixpress.common.petri

import com.wixpress.petri.experiments.domain.{Experiment, TestGroup}
import com.wixpress.petri.fakeserver.FakePetriServer
import com.wixpress.petri.test.{SampleAppRunner, TestBuilders}

class MainAmplitudeSampleApp

object MainAmplitudeSampleApp {
  def main(args: Array[String]) {
    val webappPath = classOf[AmplitudeTestappConfig].getResource("/").getPath + "../../../petri-amplitude-testapp/src/main/webapp"
    val appRunner = new SampleAppRunner(9811, webappPath, 1, true)
    val petriDriver = new PetriDriver()
    val experiment = petriDriver.addSpecAndExperiment("BUTTON_COLOR_SPEC")

    petriDriver.updateExperiment(experiment, new TestGroup(1, 50, "red"), new TestGroup(2, 50, "blue"))

    petriDriver.start()
    appRunner.start()
    println("Open your browser and go to http://localhost:9811/test to see a live experiment!")
  }
}

class PetriDriver() {
  private val petri = new FakePetriServer(9010, 9811)

  def start() = petri.start()

  def stop() = petri.stop()

  def addSpecAndExperiment(key: String) = {
    petri.addSpec(TestBuilders.abSpecBuilder(key))
    petri.addExperiment(TestBuilders.experimentWithFirstWinning(key).withScope("SampleAppFTW"))
  }

  def updateExperiment(experiment: Experiment, testGroups: TestGroup*) = {
    petri.updateExperiment(TestBuilders.updateExperimentState(experiment, testGroups: _*))
  }
}

