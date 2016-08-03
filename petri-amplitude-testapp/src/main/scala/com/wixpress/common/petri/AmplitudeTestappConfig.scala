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

    def property(property: String) =
      Source.fromFile(propertiesPath).getLines().find(line => line.startsWith(property)).map(_.split("=").last).orNull

    AmplitudeAdapter.create(property("amplitude.url"), property("amplitude.api.key"), property("amplitude.timeout.ms"))
  }


  @Bean
  @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
  def laboratory(session: HttpSession): Laboratory =
    session.getAttribute(PETRI_LABORATORY).asInstanceOf[Laboratory]
}

object AmplitudeTestappConfig {
  val webappPath = classOf[AmplitudeTestappConfig].getResource("/").getPath + "../../../petri-amplitude-testapp/src/main/webapp"
}