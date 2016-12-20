package com.wixpress.petri.google_analytics

import java.lang.reflect.Field

import com.wixpress.petri.laboratory.{BaseBiEvent, BiAdapter, BiAdapterBuilder, BiPetriEvent, FailedToPostBiEventException}

case class GoogleAnalyticsAdapter(url: String, key: String, timeoutMs: Int) extends BiAdapter {

  override def adapterType: String = "GoogleAnalytics"

  override def getBodyString(event: BaseBiEvent): String = {
    event match {
      case event: BiPetriEvent => s"v=1&tid=$key&t=event${convertPetriEventToGoogleAnalyticsUrlParameters(event)}"
      case event: BaseBiEvent => s"v=1&tid=$key&t=event${convertBaseBiEventToGoogleAnalyticsParameters(event)}"
      case _ => throw new FailedToPostBiEventException(0, adapterType)
    }
  }

  private def convertBaseBiEventToGoogleAnalyticsParameters(event: BaseBiEvent): String = {
    event.getClass.getDeclaredFields.map { field: Field => {
      field.setAccessible(true)
      val fieldValue = field.get(event).asInstanceOf[String]
      field.getName match {
        case "userId" => s"&cid=$fieldValue&uid=$fieldValue"
        case "eventType" => s"&el=$fieldValue"
        case "ip" => s"&uip=$fieldValue"
        case "language" => s"&ul=$fieldValue"
        case "country" => s"&geoid=$fieldValue"
      }
    }
    }.mkString("")
  }

  private def convertPetriEventToGoogleAnalyticsUrlParameters(event: BiPetriEvent): String = {
    s"&cid=${event.userId}&uid=${event.userId}&ec=${event.eventProperties.productName}" +
      s"&ea=${event.eventProperties.testGroup}&el=${event.eventType}&uip=${event.ip}&ua=${event.eventProperties.userAgent}" +
      s"&geoid=${event.country}&ul=${event.language}&dl=${event.eventProperties.url}"
  }
}

object GoogleAnalyticsAdapterBuilder extends BiAdapterBuilder {
  val defaultUrl = "https://www.google-analytics.com/collect"
  val defaultTrackingId = "UA-89204848-1"
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