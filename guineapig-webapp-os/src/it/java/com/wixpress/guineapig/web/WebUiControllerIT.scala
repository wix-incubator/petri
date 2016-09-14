package com.wixpress.guineapig.web

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

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

  "find js resources" in new Context {
    val response = httpDriver.getRaw("http://localhost:9901/resources/statics/scripts/scripts.js")
    val jsBody = EntityUtils.toString(response.getEntity)
    jsBody.length must be_>(5000)
  }

  "find gif resources" in new Context {
    val response = httpDriver.getRaw("http://localhost:9901/resources/statics/images/ajax-loader.gif")
    val imgArr = EntityUtils.toByteArray(response.getEntity)
    val img = ImageIO.read(new ByteArrayInputStream(imgArr))
    img.getHeight === 32
    img.getWidth === 32
  }
}