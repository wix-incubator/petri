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

    "resolve by header 'SOME_HEADER' when configured" in new Context {
      val config = FilterParametersExtractorsConfig(Map("Language" -> List(("Header", "SOME_HEADER"))))
      val someLang = "he"
      request.addHeader("SOME_HEADER", someLang)
      resolver.resolve(request, config) must beEqualTo (someLang)
    }

    "resolve by param 'SOME_PARAM' when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map("Language" ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))
      val someLang = "he"
      request.addParameter("SOME_PARAM", someLang)
      resolver.resolve(request, config) must beEqualTo (someLang)
    }

    "resolve by default behaviour when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map("Language" ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))
      resolver.resolve(request, config) must beEqualTo (request.getLocale.getLanguage)
    }
  }
}
