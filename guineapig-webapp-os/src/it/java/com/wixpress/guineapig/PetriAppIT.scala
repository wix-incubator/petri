package com.wixpress.guineapig

import com.wixpress.guineapig.drivers.SpecificationWithEnvSupport
import org.springframework.http.HttpStatus

class PetriAppIT extends SpecificationWithEnvSupport {

  "PetriApp" should {
    "redirect on HTTP call" in {
      val response = httpDriver.get("http://127.0.0.1:9901/petri/")
      response.getStatusCode === HttpStatus.FOUND
      response.getRedirectUrl === "https://127.0.0.1/petri"
    }.pendingUntilFixed("Determine whether the functionality tested in PetriAppIT is required - seems like it was previously supported by framework code (the FW DispatcherServlet has been replaced with Spring's DispatcherServlet)")

    "render on HTTPS call" in {
      val response = httpDriver.get("https://127.0.0.1:9901/petri/")
      response.getStatusCode === HttpStatus.OK
      response.getBodyRaw === "https://127.0.0.1/petri"
    } // this test was ignored in the original project as well
      .pendingUntilFixed("Ignoring this new test until i will add support for making ssl calls to the test framework ")
  }
}
