package com.wixpress.common.petri

import com.wixpress.common.petri.AmplitudeTestappConfig.webappPath
import com.wixpress.petri.experiments.domain.{Experiment, TestGroup}
import com.wixpress.petri.fakeserver.FakePetriServer
import com.wixpress.petri.test.{SampleAppRunner, TestBuilders}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

class AmplitudeSampleAppRunner extends SpecificationWithJUnit with BeforeAfterAll {
  val appRunner = new SampleAppRunner(9811, webappPath, 1, true)
  val petriDriver = new PetriDriver(9010)

  "AmplitudeTestapp" should {
    "run the app" in {
      val experiment = petriDriver.addSpecAndExperiment("BUTTON_COLOR_SPEC")
      petriDriver.updateExperiment(experiment, new TestGroup(1, 50, "a"), new TestGroup(2, 50, "b"))
      Thread.sleep(999999999)
      ok
    }
  }

  override def beforeAll(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        petriDriver.start()
        appRunner.start()
      }
    }).start()


  }

  override def afterAll(): Unit = {
    petriDriver.stop()
    appRunner.stop()
  }

  class PetriDriver(port: Int) {
    private val petri = new FakePetriServer(port)

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

}