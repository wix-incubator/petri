package com.wixpress.petri.laboratory

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class LanguageResolverTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val resolver = LanguageResolver()
    val request = new MockHttpServletRequest
  }

  "LanguageResolver" should {
    "resolve by getLocal getLanguage with empty config" in new Context {
      val config = FilterParametersExtractorsConfig()
      resolver.resolve(request, config) must beEqualTo (request.getLocale.getLanguage)
    }
  }
}
