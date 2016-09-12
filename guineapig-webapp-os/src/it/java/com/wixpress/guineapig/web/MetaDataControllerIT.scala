package com.wixpress.guineapig.web

import com.wixpress.guineapig.drivers.SpecificationWithEnvSupport
import org.specs2.specification.Scope


class MetaDataControllerIT extends SpecificationWithEnvSupport {

  trait Context extends Scope


  "MetaData controller" should {
    "fetch languages" in new Context {
      httpDriver.get("http://localhost:9901/v1/languages").getBodyRaw must
        (contain("en") and contain("English") and contain("de") and contain("German"))
    }

  }
}
