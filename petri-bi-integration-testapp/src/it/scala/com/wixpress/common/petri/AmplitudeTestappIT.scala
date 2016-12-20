package com.wixpress.common.petri

import com.github.tomakehurst.wiremock.client.VerificationException
import com.wixpress.petri.laboratory.BiPetriEvent
import com.wixpress.petri.test.SampleAppRunner

class AmplitudeTestappIT extends BaseTestapp {
  val sampleAppViewDriver = new SampleAppViewDriver(webappPort, "testAmplitude")
  val amplitudeUrl = s"http://localhost:$biServerPort/httpapi"
  val appRunner = new SampleAppRunner(webappPort, webappPath, 1, true, amplitudeUrl, null)

  override def beforeAll(): Unit = {
    super.beforeAll()
    appRunner.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    appRunner.stop()
  }

  "AmplitudeTestapp" should {
    "enter the page, click the button and check that petri event + business bi event were logged in amplitude" in {
      petriDriver.addSpecAndExperiment("BUTTON_COLOR_SPEC")

      sampleAppViewDriver.enterThePageAndClickButton()

      eventually {
        biServerDriver.assertThatBiServerWasCalledWith(partialBody = ButtonClickedEvent.eventType, "httpapi") must not(throwA[VerificationException])
        biServerDriver.assertThatBiServerWasCalledWith(partialBody = BiPetriEvent.petriBiEventType,  "httpapi") must not(throwA[VerificationException])
      }
    }
  }



}
