package com.wixpress.common.petri

import javax.servlet.http.HttpSession

import com.wixpress.petri.amplitude.AmplitudeAdapterBuilder
import com.wixpress.petri.google_analytics.GoogleAnalyticsAdapterBuilder
import com.wixpress.petri.laboratory.Laboratory
import com.wixpress.petri.laboratory.http.LaboratoryFilter._
import org.springframework.context.annotation.{Bean, Configuration, Scope, ScopedProxyMode}

import scala.io.Source


@Configuration
class AmplitudeTestappConfig {
  @Bean
  def amplitudeAdapter = {
    val propertiesPath = AmplitudeTestappConfig.webappPath + "/WEB-INF/laboratory.properties"

    def property(property: String) =
      Source.fromFile(propertiesPath).getLines().find(line => line.startsWith(property)).map(_.split("=").last).orNull

    AmplitudeAdapterBuilder.create(property("amplitude.url"), property("amplitude.api.key"), property("amplitude.timeout.ms"))
  }

  @Bean
  def googleAnalyticsAdapter = {
    val propertiesPath = AmplitudeTestappConfig.webappPath + "/WEB-INF/laboratory.properties"

    def property(property: String) =
      Source.fromFile(propertiesPath).getLines().find(line => line.startsWith(property)).map(_.split("=").last).orNull

    GoogleAnalyticsAdapterBuilder.create(property("google.analytics.url"), property("google.analytics.tracking.id"), property("google.analytics.timeout.ms"))
  }

  @Bean
  @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
  def laboratory(session: HttpSession): Laboratory =
    session.getAttribute(PETRI_LABORATORY).asInstanceOf[Laboratory]
}

object AmplitudeTestappConfig {
  val webappPath = classOf[AmplitudeTestappConfig].getResource("/").getPath + "../../../petri-bi-integration-testapp/src/main/webapp"
}