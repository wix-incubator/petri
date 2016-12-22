package com.wixpress.petri.google_analytics

import java.lang.reflect.Field

import com.wixpress.petri.laboratory.{BaseBiEvent, BiAdapter, BiAdapterBuilder, PetriEventProperties}

case class GoogleAnalyticsAdapter(url: String, key: String, timeoutMs: Int) extends BiAdapter {

  val googleAnalyticsFieldsMap = Map("eventType" -> "el", "ip" -> "uip", "language" -> "ul", "country" -> "geoid")

  override def adapterType: String = "GoogleAnalytics"

  override def getBodyString(event: BaseBiEvent): String =
    s"v=1&tid=$key&t=event${convertBaseBiEventToGoogleAnalyticsParameters(event)}"

  private def convertBaseBiEventToGoogleAnalyticsParameters(event: BaseBiEvent): String = {
    event.getClass.getDeclaredFields.map { field: Field => {
      field.setAccessible(true)
      field.getName match {
        case "eventProperties" => val fieldValue = field.get(event).asInstanceOf[PetriEventProperties]
          s"&ec=${fieldValue.productName}&ea=${fieldValue.testGroup}&ua=${fieldValue.userAgent}&dl=${fieldValue.url}"
        case "userId" => val fieldValue = field.get(event).asInstanceOf[String]
          s"&cid=$fieldValue&uid=$fieldValue"
        case name => val fieldValue = field.get(event).asInstanceOf[String]
          s"&${googleAnalyticsFieldsMap.getOrElse(name, name)}=$fieldValue"
      }
    }
    }.mkString("")
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
