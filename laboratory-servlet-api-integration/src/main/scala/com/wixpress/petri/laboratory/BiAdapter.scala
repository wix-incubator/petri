package com.wixpress.petri.laboratory

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder


trait BiAdapterBuilder {
  def create(key: String, url: String, timeoutMs: String): BiAdapter

  def isAllDigits(x: String) = x forall Character.isDigit
}

trait BiAdapter {
  val key: String
  val url: String
  val timeoutMs: Int

  def adapterType: String

  val requestConfig = RequestConfig.custom.
    setConnectionRequestTimeout(timeoutMs).
    setConnectTimeout(timeoutMs).
    setSocketTimeout(timeoutMs * 2).build

  def getBodyString(event: BaseBiEvent): String

  def sendEvent(event: BaseBiEvent): Unit = {
    val client = HttpClientBuilder.create.setDefaultRequestConfig(requestConfig).build
    val httpPost = new HttpPost(url)
    val bodyStr = getBodyString(event)
    val entity = new ByteArrayEntity(bodyStr.getBytes("UTF-8"))
    httpPost.setEntity(entity)

    val response = client.execute(httpPost)
    val resultCode = response.getStatusLine.getStatusCode

    if (resultCode != 200) {
      throw new FailedToPostBiEventException(resultCode, adapterType)
    }
  }
}

trait BaseBiEvent {
  def eventType: String

  def userId: String
}

class FailedToPostBiEventException(resultCode: Int, adapterType: String)
  extends Exception(s"Failed to post bi event to $adapterType - returned code $resultCode")
