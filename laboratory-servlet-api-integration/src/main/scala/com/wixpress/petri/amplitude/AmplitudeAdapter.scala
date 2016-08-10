package com.wixpress.petri.amplitude

import javax.annotation.Nullable

import com.wixpress.petri.utils.JsonMapper._
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder

case class AmplitudeAdapter(amplitudeUrl: String, apiKey: String, amplitudeTimeoutMs: Int) {
  def this(amplitudeUrl: String, apiKey: String) = this(amplitudeUrl, apiKey, 2000)

  val requestConfig = RequestConfig.custom.
    setConnectionRequestTimeout(amplitudeTimeoutMs).
    setConnectTimeout(amplitudeTimeoutMs).
    setSocketTimeout(amplitudeTimeoutMs * 2).build

  def sendEvent(event: BaseAmplitudeEvent): Unit = {
    val client = HttpClientBuilder.create.setDefaultRequestConfig(requestConfig).build
    val httpPost = new HttpPost(amplitudeUrl)
    val bodyStr = s"api_key=$apiKey&event=${asJson(event)}"
    val entity = new ByteArrayEntity(bodyStr.getBytes("UTF-8"))
    httpPost.setEntity(entity)

    val response = client.execute(httpPost)
    val resultCode = response.getStatusLine.getStatusCode

    if (resultCode != 200) {
      throw new FailedToPostAmplitudeEventException(resultCode)
    }
  }
}

object AmplitudeAdapter {
  val defaultUrl = "https://amplitude.com/httpapi"
  val defaultApiKey = "198e3469868de498f5d67581d6de4518"
  val defaultTimeout = 2000

  def create(@Nullable url: String, @Nullable apiKey: String, @Nullable timeoutMs: String): AmplitudeAdapter = {
    def isAllDigits(x: String) = x forall Character.isDigit

    val maybeUrl = Option(url)
    val maybeApiKey = Option(apiKey)
    val maybeTimeout = Option(timeoutMs).filter(isAllDigits).map(_.toInt)

    val amplitudeUrl = maybeUrl.getOrElse(defaultUrl)
    val amplitudeApiKey = maybeApiKey.getOrElse(defaultApiKey)

    maybeTimeout match {
      case Some(timeout) => new AmplitudeAdapter(amplitudeUrl, amplitudeApiKey, timeout)
      case _ => new AmplitudeAdapter(amplitudeUrl, amplitudeApiKey)
    }
  }
}


trait BaseAmplitudeEvent {
  def eventType: String

  def userId: String
}

class FailedToPostAmplitudeEventException(resultCode: Int) extends Exception(s"Failed to post bi event to amplitude - returned code $resultCode")