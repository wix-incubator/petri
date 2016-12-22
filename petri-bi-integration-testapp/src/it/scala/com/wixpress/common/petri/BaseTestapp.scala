package com.wixpress.common.petri

import java.util.Arrays._

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.wixpress.petri.experiments.domain.TestGroup
import com.wixpress.petri.fakeserver.FakePetriServer
import com.wixpress.petri.laboratory.BiServerDriver
import com.wixpress.petri.test.TestBuilders
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebElement}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

abstract class BaseTestapp extends SpecificationWithJUnit with BeforeAfterAll {
  val webappPath = classOf[BaseTestapp].getResource("/").getPath + "../../../petri-bi-integration-testapp/src/main/webapp"
  val webappPort = 9811
  val biServerPort = 11981
  val petriServerPort = 9010
  val biServerDriver = new BiServerDriver(biServerPort)
  val petriDriver = new PetriDriver()

  override def beforeAll(): Unit = {
    petriDriver.start()
    biServerDriver.start()
  }

  override def afterAll(): Unit = {
    biServerDriver.stop()
    petriDriver.stop()
  }

  class SampleAppViewDriver(port: Int, pageUrlPath: String) {
    private val driver = new HtmlUnitDriver(BrowserVersion.CHROME, true)
    private val pageUrl = s"http://localhost:$port/$pageUrlPath"

    def enterThePageAndClickButton() = {
      driver.get(pageUrl)
      val button = driver.findElement(By.id("buttonId"))
      assertButtonWasNotRenderedOnFallbackColor(button)

      button.click()

      driver.findElement(By.id("resultText")).getText must eventually(contain("Finished"))
    }
  }

  def assertButtonWasNotRenderedOnFallbackColor(button: WebElement) = {
    button.getAttribute("style") must contain("color: red")
  }

  class PetriDriver() {
    private val petri = new FakePetriServer(petriServerPort, webappPort)

    def start() = petri.start()

    def stop() = petri.stop()

    def addSpecAndExperiment(key: String) = {
      petri.addSpec(TestBuilders.abSpecBuilder(key))

      petri.addExperiment(TestBuilders.experimentWithFirstWinning(key).
        withGroups(asList(new TestGroup(1, 100, "red"), new TestGroup(2, 0, "blue"))))
    }
  }
}
