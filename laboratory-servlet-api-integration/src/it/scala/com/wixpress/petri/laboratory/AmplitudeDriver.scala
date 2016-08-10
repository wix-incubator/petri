package com.wixpress.petri.laboratory

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.SingleRootFileSource
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader

class AmplitudeDriver(port: Int) {
  private val amplitudeWiremock = new AmplitudeWiremock(port)

  val amplitudeUrl = s"http://localhost:$port/httpapi"

  def start() = amplitudeWiremock.start()

  def stop() = amplitudeWiremock.stop()

  def assertThatAmplitudeWasCalled() = {
    amplitudeWiremock.verify(postRequestedFor(urlPathEqualTo("/httpapi")))
  }

  def assertThatAmplitudeWasCalledWith(partialBody: String) = {
    amplitudeWiremock.verify(postRequestedFor(urlPathEqualTo("/httpapi")).withRequestBody(containing(partialBody)))
  }
}

private class AmplitudeWiremock(port: Int) extends WireMockServer(port) {
  override def start(): Unit = {
    loadMappingsUsing(new JsonFileMappingsLoader(new SingleRootFileSource("target/test-classes/")))
    super.start()
  }
}