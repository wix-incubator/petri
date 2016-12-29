package com.wixpress.petri.laboratory

import java.util.Base64
import javax.servlet.http.{Cookie, HttpServletRequest}

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class Base64Converter extends Converter[String] {
  def convert(value: String): String = new String(Base64.getDecoder.decode(value.getBytes))
}

class ResolversTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val aDefaultResolution = "default"
    val aFilterParam: FilterParameter = LanguageFilterParameter
    val someValue = "someVal"

    val resolver = new StringResolver {
      override def defaultResolution(request: HttpServletRequest): String = aDefaultResolution
      override val filterParam: FilterParameter = aFilterParam
    }

    val request = new MockHttpServletRequest

    def configFor(extractorsConfig: Map[FilterParameter, List[(HttpRequestExtractionOption, String)]] = Map.empty,
                  customConverters: Map[FilterParameter, Converter[_]] = Map.empty) = FilterParametersConfig(extractorsConfig, customConverters)

    val emptyConfig = configFor()
  }


  "Resolver" should {
    "resolve by default resolution when configs empty" in new Context {
      resolver.resolve(request, emptyConfig) must beEqualTo(aDefaultResolution)
    }

    "resolve by header 'SOME_HEADER' when configured" in new Context {
      val config = configFor(extractorsConfig = Map(aFilterParam -> List((HeaderExtractionOption, "SOME_HEADER"))))

      request.addHeader("SOME_HEADER", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by 'SOME_COOKIE_NAME' cookie if filterParamConfig defines filterParam Country cookie" in new Context {
      val config = configFor(extractorsConfig = Map(aFilterParam -> List((CookieExtractionOption, "SOME_COOKIE_NAME"))))

      request.setCookies(new Cookie("SOME_COOKIE_NAME", someValue))

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by param 'SOME_PARAM' when configuration before that are not present" in new Context {
      val config = configFor(extractorsConfig =
        Map(aFilterParam ->
          List((HeaderExtractionOption, "SOME_HEADER"),
            (CookieExtractionOption, "SOME_COOKIE"),
            (ParamExtractionOption, "SOME_PARAM"))))

      request.addParameter("SOME_PARAM", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by configuration order if first config is missing move to next" in new Context {
      val config = configFor(extractorsConfig =
        Map(aFilterParam ->
          List((CookieExtractionOption, "SOME_COOKIE_NAME"),
            (HeaderExtractionOption, "Some_Header"))))

      request.addHeader("Some_Header", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by default behaviour when configuration before that are not present" in new Context {
      val config = configFor(extractorsConfig =
        Map(aFilterParam ->
          List((HeaderExtractionOption, "SOME_HEADER"),
            (CookieExtractionOption, "SOME_COOKIE"),
            (ParamExtractionOption, "SOME_PARAM"))))

      resolver.resolve(request, config) must beEqualTo(aDefaultResolution)
    }

    "resolve by first applicable configuration -> Param" in new Context {
      val config = configFor(extractorsConfig =
        Map(aFilterParam ->
          List((CookieExtractionOption, "SOME_COOKIE_NAME"),
            (ParamExtractionOption, "Some_Param"),
            (HeaderExtractionOption, "Some_Header"))))

      request.addHeader("Some_Header", "someOtherValue")
      request.addParameter("Some_Param", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "be able to resolve different types than String" in new Context {

       val intResolver = new Resolver[Integer] {
        override val filterParam: FilterParameter = aFilterParam
        override def convert(value: String): Integer = Integer.parseInt(value)
        override def defaultResolution(request: HttpServletRequest): String = "1"
      }

      val config = configFor(extractorsConfig = Map(aFilterParam -> List((HeaderExtractionOption, "SOME_HEADER"))))
      request.addHeader("SOME_HEADER", "999")

      intResolver.resolve(request, config) must beEqualTo(999)
    }

    "resolve by some type with custom converter" in new Context {

      val extractorsConfig = Map(aFilterParam -> List((HeaderExtractionOption, "SOME_HEADER")))
      val customConverters =Map(aFilterParam -> new Base64Converter())
      val config = configFor(extractorsConfig, customConverters)

      request.addHeader("SOME_HEADER", new String(Base64.getEncoder.encode(someValue.getBytes)))

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by default resolution and custom converter" in new Context {

      override val resolver = new StringResolver {
        override def defaultResolution(request: HttpServletRequest): String = new String(Base64.getEncoder.encode(someValue.getBytes))
        override val filterParam: FilterParameter = aFilterParam
      }

      val config = configFor(customConverters = Map(aFilterParam -> new Base64Converter()))

      resolver.resolve(request, config)  must beEqualTo(someValue)
    }

    "resolve properly null values when converter does not handle null properly" in new Context {
      override val resolver = new StringResolver {
        override def defaultResolution(request: HttpServletRequest): String = null
        override val filterParam: FilterParameter = aFilterParam
        override def convert(value: String): String = {
          if (value == null) throw new RuntimeException("cannot handle null value") else value
        }
      }

      resolver.resolve(request, emptyConfig) must not(throwA[Exception])
    }
  }
}