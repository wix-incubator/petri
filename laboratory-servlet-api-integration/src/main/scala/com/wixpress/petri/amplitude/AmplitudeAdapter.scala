package com.wixpress.petri.amplitude

import com.wixpress.petri.laboratory.{BaseBiEvent, BiAdapter, BiAdapterBuilder}
import com.wixpress.petri.utils.JsonMapper._

case class AmplitudeAdapter(url: String, key: String, timeoutMs: Int) extends BiAdapter {

  override def adapterType: String = "Amplitude"

  override def getBodyString(event: BaseBiEvent): String = s"api_key=$key&event=${asJson(event)}"
}

object AmplitudeAdapterBuilder extends BiAdapterBuilder {
  val defaultUrl = "https://amplitude.com/httpapi"
  val defaultApiKey = "198e3469868de498f5d67581d6de4518"
  val defaultTimeout = 2000

  override def create(url: String, key: String, timeoutMs: String): BiAdapter = {
    val maybeUrl = Option(url)
    val maybeApiKey = Option(key)
    val maybeTimeout = Option(timeoutMs).filter(isAllDigits).map(_.toInt)

    val amplitudeUrl = maybeUrl.getOrElse(defaultUrl)
    val amplitudeKey = maybeApiKey.getOrElse(defaultApiKey)
    val amplitudeTimeoutMs = maybeTimeout.getOrElse(defaultTimeout)

    AmplitudeAdapter(amplitudeUrl, amplitudeKey, amplitudeTimeoutMs)
  }
}
