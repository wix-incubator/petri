package com.wixpress.guineapig.drivers

import java.io.StringWriter

import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpEntityEnclosingRequestBase, HttpGet, HttpPost, HttpPut}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

class HttpDriver {

  val client: HttpClient = HttpClientBuilder.create.build

  val om = ObjectMapperFactory.makeObjectMapper()

  def get(uri: String): JsonResponse = {
    val request = new HttpGet(uri)
    request.setHeader("Accept", "application/json")
    val response: HttpResponse = client.execute(request)
    new JsonResponse(EntityUtils.toString(response.getEntity, "UTF-8"))
  }

  def post(uri: String): JsonResponse = {
    val postMethod = new HttpPost(uri)
    new JsonResponse(EntityUtils.toString(client.execute(postMethod).getEntity, "UTF-8"))
  }

  def post[T](uri: String, jsonPayload: T): JsonResponse = {
    executeMethod(new HttpPost(uri), jsonPayload)
  }

  def put[T](uri: String, jsonPayload: T): JsonResponse = {
    executeMethod(new HttpPut(uri), jsonPayload)
  }
  
  def getRaw(uri: String): HttpResponse = {
    val httpMethod = new HttpGet(uri)
    client.execute(httpMethod)
  }

  private def executeMethod[T](httpMethod: HttpEntityEnclosingRequestBase, jsonPayload: T): JsonResponse = {
    val stringWriter = new StringWriter()
    om.writeValue(stringWriter, jsonPayload)
    val requestEntity = new StringEntity(stringWriter.toString, ContentType.APPLICATION_JSON)

    httpMethod.setEntity(requestEntity)
    httpMethod.setHeader("Accept", "application/json")
    httpMethod.setHeader("Content-Type", "application/json")

    new JsonResponse(EntityUtils.toString(client.execute(httpMethod).getEntity, "UTF-8"))
  }
}
