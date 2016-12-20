package com.wixpress.common.petri

import com.github.tomakehurst.wiremock.client.VerificationException
import com.wixpress.petri.laboratory.BiPetriEvent
import com.wixpress.petri.test.SampleAppRunner

class GoogleAnalyticsTestappIT extends BaseTestapp {

  val sampleAppViewDriver = new SampleAppViewDriver(webappPort, "testGoogleAnalytics")
  val googleAnalyticsUrl = s"http://localhost:$biServerPort/collect"
  val appRunner = new SampleAppRunner(webappPort, webappPath, 1, true, null, googleAnalyticsUrl)

  override def beforeAll(): Unit = {
    super.beforeAll()
    appRunner.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    appRunner.stop()
  }
  "GoogleAnalyticsTestapp" should {
    "enter the page, click the button and check that petri event + business bi event were logged in google analytics" in {
      petriDriver.addSpecAndExperiment("BUTTON_COLOR_SPEC")

      sampleAppViewDriver.enterThePageAndClickButton()

      eventually {
        biServerDriver.assertThatBiServerWasCalledWith(partialBody = ButtonClickedEvent.eventType, "collect") must not(throwA[VerificationException])
        biServerDriver.assertThatBiServerWasCalledWith(partialBody = BiPetriEvent.petriBiEventType, "collect") must not(throwA[VerificationException])
      }
    }
  }


}
