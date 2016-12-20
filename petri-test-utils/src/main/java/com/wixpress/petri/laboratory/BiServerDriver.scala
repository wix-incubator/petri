package com.wixpress.petri.laboratory

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.common.SingleRootFileSource
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader

class BiServerDriver(port: Int, urlPath: String) {
  private val biServerWireMock = new BiServerWireMock(port)

  val biServerUrl = s"http://localhost:$port/$urlPath"

  def start() = biServerWireMock.start()

  def stop() = biServerWireMock.stop()

  def assertThatBiServerWasCalled() = {
    biServerWireMock.verify(postRequestedFor(urlPathEqualTo(s"/$urlPath")))
  }

  def assertThatBiServerWasCalledWith(partialBody: String) = {
    biServerWireMock.verify(postRequestedFor(urlPathEqualTo(s"/$urlPath")).withRequestBody(containing(partialBody)))
  }
}

private class BiServerWireMock(port: Int) extends WireMockServer(port) {
  override def start(): Unit = {
    loadMappingsUsing(new JsonFileMappingsLoader(new SingleRootFileSource(s"target/test-classes")))
    super.start()
  }
}
