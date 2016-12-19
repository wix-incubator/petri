package com.wixpress.petri.google_analytics

import com.wixpress.petri.laboratory.{BaseBiEvent, BiAdapter, BiAdapterBuilder, BiPetriEvent, FailedToPostBiEventException}

case class GoogleAnalyticsAdapter(url: String, key: String, timeoutMs: Int) extends BiAdapter {

  override def adapterType: String = "GoogleAnalytics"

  override def getBodyString(event: BaseBiEvent): String = {
    event match {
      case event: BiPetriEvent => s"v=1&tid=$key${convertPetriEventToGoogleAnalyticsUrlParameters(event)}"
      case _ => throw new FailedToPostBiEventException(0, adapterType)
    }
  }

  private def convertPetriEventToGoogleAnalyticsUrlParameters(event: BiPetriEvent): String = {
    s"&cid=${event.userId}&uid=${event.userId}&t=event&ec=${event.eventProperties.productName}" +
      s"&ea=${event.eventProperties.testGroup}&el=${event.eventType}&uip=${event.ip}&ua=${event.eventProperties.userAgent}" +
      s"&geoid=${event.country}&ul=${event.language}&dl=${event.eventProperties.url}"
  }
}

object GoogleAnalyticsAdapterBuilder extends BiAdapterBuilder {
  val defaultUrl = "https://www.google-analytics.com/collect"
  val defaultTrackingId = "UA-89180922-1"
  val defaultTimeout = 2000

  override def create(url: String, key: String, timeoutMs: String): BiAdapter = {
    val maybeUrl = Option(url)
    val maybeApiKey = Option(key)
    val maybeTimeout = Option(timeoutMs).filter(isAllDigits).map(_.toInt)

    val amplitudeUrl = maybeUrl.getOrElse(defaultUrl)
    val amplitudeKey = maybeApiKey.getOrElse(defaultTrackingId)
    val amplitudeTimeoutMs = maybeTimeout.getOrElse(defaultTimeout)

    GoogleAnalyticsAdapter(amplitudeUrl, amplitudeKey, amplitudeTimeoutMs)
  }
}