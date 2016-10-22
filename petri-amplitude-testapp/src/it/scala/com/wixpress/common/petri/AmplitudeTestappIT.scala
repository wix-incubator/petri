package com.wixpress.common.petri

import java.util.Arrays._

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.github.tomakehurst.wiremock.client.VerificationException
import com.wixpress.petri.experiments.domain.TestGroup
import com.wixpress.petri.fakeserver.FakePetriServer
import com.wixpress.petri.laboratory.{AmplitudeDriver, AmplitudePetriEvent}
import com.wixpress.petri.test.{SampleAppRunner, TestBuilders}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebElement}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

class AmplitudeTestappIT extends SpecificationWithJUnit with BeforeAfterAll {
  val webappPath = classOf[AmplitudeTestappIT].getResource("/").getPath + "../../../petri-amplitude-testapp/src/main/webapp"
  val webappPort = 9811
  val amplitudePort = 11981
  val amplitudeDriver = new AmplitudeDriver(amplitudePort)
  val appRunner = new SampleAppRunner(webappPort, webappPath, 1, true, amplitudeDriver.amplitudeUrl)
  val sampleAppViewDriver = new SampleAppViewDriver(webappPort)
  val petriDriver = new PetriDriver(9010)

  "AmplitudeTestapp" should {
    "enter the page, click the button and check that petri event + business bi event were logged in amplitude" in {
      petriDriver.addSpecAndExperiment("BUTTON_COLOR_SPEC")

      sampleAppViewDriver.enterThePageAndClickButton()

      eventually {
        amplitudeDriver.assertThatAmplitudeWasCalledWith(partialBody = ButtonClickedEvent.eventType) must not(throwA[VerificationException])
        amplitudeDriver.assertThatAmplitudeWasCalledWith(partialBody = AmplitudePetriEvent.petriBiEventType) must not(throwA[VerificationException])
      }
    }
  }

  override def beforeAll(): Unit = {
    appRunner.start()
    petriDriver.start()
    amplitudeDriver.start()
  }

  override def afterAll(): Unit = {
    amplitudeDriver.stop()
    petriDriver.stop()
    appRunner.stop()
  }

  class SampleAppViewDriver(port: Int) {
    private val driver = new HtmlUnitDriver(BrowserVersion.CHROME, true)
    private val pageUrl = s"http://localhost:$port/test"

    def enterThePageAndClickButton() = {
      driver.get(pageUrl)
      val button = driver.findElement(By.id("buttonId"))
      assertButtonWasNotRenderedOnFallbackColor(button)

      button.click()

      driver.findElement(By.id("resultText")).getText must eventually(contain("Finished"))
    }
  }

  def assertButtonWasNotRenderedOnFallbackColor(button: WebElement) =
    button.getAttribute("style") must contain("color: red")

  class PetriDriver(port: Int) {
    private val petri = new FakePetriServer(port)

    def start() = petri.start()

    def stop() = petri.stop()

    def addSpecAndExperiment(key: String) = {
      petri.addSpec(TestBuilders.abSpecBuilder(key))

      petri.addExperiment(TestBuilders.experimentWithFirstWinning(key).withScope("SampleAppFTW").
        withGroups(asList(new TestGroup(1, 100, "red"), new TestGroup(2, 0, "blue"))))
    }
  }

}





