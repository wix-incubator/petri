package com.wixpress.guineapig.web

import com.wixpress.guineapig.drivers.SpecificationWithEnvSupport
import org.apache.http.util.EntityUtils
import org.specs2.specification.Scope

class WebUiControllerIT extends SpecificationWithEnvSupport {

  trait Context extends Scope

  "get index vm" in new Context {
    val response = httpDriver.getRaw("http://localhost:9901/v1/index")
    response.getStatusLine.getStatusCode === 200
  }

  "not contain velocity parameters" in new Context {
    val response = httpDriver.getRaw("http://localhost:9901/v1/index")
    val htmlBody = EntityUtils.toString(response.getEntity)
    htmlBody must not contain("${")
  }
}
