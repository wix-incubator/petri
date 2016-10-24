package com.wixpress.guineapig.drivers

import com.gargoylesoftware.htmlunit.{SilentCssErrorHandler, WebClient}
import org.openqa.selenium.By
import org.openqa.selenium.htmlunit.HtmlUnitDriver


class PetriBackofficeUiDriver(port: Int) extends org.specs2.mutable.Specification{
  private val driver = new NoCssErrorsHtmlUnitDriver()
  private val pageUrl = s"http://localhost:$port/v1/index#/login/home/active"

  def numberOfActiveExperimentsIs(num: Int) = {
    driver.get(pageUrl)
    driver.findElement(By.id("mainView")).getText must eventually(contain(s"active Experiments ($num)"))
  }

  class NoCssErrorsHtmlUnitDriver extends HtmlUnitDriver(true){

    override def modifyWebClient(client: WebClient): WebClient = {
      client.setCssErrorHandler(new SilentCssErrorHandler)
      client
    }
  }

}
