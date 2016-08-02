package com.wixpress.common.petri

import javax.servlet.http.HttpSession

import com.wixpress.petri.laboratory.http.LaboratoryFilter._
import com.wixpress.petri.laboratory.{AmplitudeAdapter, Laboratory}
import org.springframework.context.annotation.{Bean, Configuration, Scope, ScopedProxyMode}

import scala.io.Source


@Configuration
class AmplitudeTestappConfig {
  @Bean
  def amplitudeAdapter = {
    val propertiesPath = AmplitudeTestappConfig.webappPath + "/WEB-INF/laboratory.properties"
    val amplitudeUrlFromConfig = Source.fromFile(propertiesPath).getLines().find(line => line.startsWith("amplitude.url")).map(_.split("=").last)

    new AmplitudeAdapter(
      amplitudeUrl = amplitudeUrlFromConfig.getOrElse("https://api.amplitude.com/httpapi"),
      apiKey = "198e3469868de498f5d67581d6de4518")
  }


  @Bean
  @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
  def laboratory(session: HttpSession): Laboratory =
    session.getAttribute(PETRI_LABORATORY).asInstanceOf[Laboratory]
}

object AmplitudeTestappConfig {
  val webappPath = classOf[AmplitudeTestappConfig].getResource("/").getPath + "../../../petri-amplitude-testapp/src/main/webapp"
}