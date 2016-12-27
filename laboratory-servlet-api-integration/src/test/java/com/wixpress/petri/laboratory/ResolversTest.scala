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
    val aFilterParam = FilterParameters.Language
    val someValue = "someVal"

    val resolver = new StringResolver {
      override def defaultResolution(request: HttpServletRequest): String = aDefaultResolution
      override val filterParam: FilterParameters.Value = aFilterParam
    }

    val request = new MockHttpServletRequest
    val emptyCustomConverters = CustomConverters()
    val emptyConfig = FilterParametersExtractorsConfig()
  }


  "Resolver" should {
    "resolve by default resolution when configs empty" in new Context {
      resolver.resolve(request, emptyConfig, emptyCustomConverters) must beEqualTo(aDefaultResolution)
    }

    "resolve by header 'SOME_HEADER' when configured" in new Context {
      val config = FilterParametersExtractorsConfig(Map(aFilterParam.toString -> List(("Header", "SOME_HEADER"))))

      request.addHeader("SOME_HEADER", someValue)

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(someValue)
    }

    "resolve by 'SOME_COOKIE_NAME' cookie if filterParamConfig defines filterParam Country cookie" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString -> List(("Cookie", "SOME_COOKIE_NAME"))))

      request.setCookies(new Cookie("SOME_COOKIE_NAME", someValue))

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(someValue)
    }

    "resolve by param 'SOME_PARAM' when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))

      request.addParameter("SOME_PARAM", someValue)

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(someValue)
    }

    "resolve by configuration order if first config is missing move to next" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Cookie", "SOME_COOKIE_NAME"), ("Header", "Some_Header"))))

      request.addHeader("Some_Header", someValue)

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(someValue)
    }

    "resolve by default behaviour when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(aDefaultResolution)
    }

    "resolve by first applicable configuration -> Param" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Cookie", "SOME_COOKIE_NAME"), ("Param", "Some_Param"), ("Header", "Some_Header"))))

      request.addHeader("Some_Header", "someOtherValue")
      request.addParameter("Some_Param", someValue)

      resolver.resolve(request, config, emptyCustomConverters) must beEqualTo(someValue)
    }

    "be able to resolve different types than String" in new Context {

       val intResolver = new Resolver[Integer] {
        override val filterParam: FilterParameters.Value = aFilterParam
        override def convert(value: String): Integer = Integer.parseInt(value)
        override def defaultResolution(request: HttpServletRequest): String = "1"
      }

      val config = FilterParametersExtractorsConfig(Map(aFilterParam.toString -> List(("Header", "SOME_HEADER"))))
      request.addHeader("SOME_HEADER", "999")

      intResolver.resolve(request, config, emptyCustomConverters) must beEqualTo(999)
    }

    "resolve by some type with custom converter" in new Context {

      val config = FilterParametersExtractorsConfig(Map(aFilterParam.toString -> List(("Header", "SOME_HEADER"))))
      val customConverters = CustomConverters(Map(aFilterParam.toString -> new Base64Converter()))

      request.addHeader("SOME_HEADER", new String(Base64.getEncoder.encode(someValue.getBytes)))

      resolver.resolve(request, config, customConverters) must beEqualTo(someValue)
    }

    "resolve by default resolution and custom converter" in new Context {

      override val resolver = new StringResolver {
        override def defaultResolution(request: HttpServletRequest): String = new String(Base64.getEncoder.encode(someValue.getBytes))
        override val filterParam: FilterParameters.Value = aFilterParam
      }

      val customConverters = CustomConverters(Map(aFilterParam.toString -> new Base64Converter()))

      resolver.resolve(request, emptyConfig, customConverters)  must beEqualTo(someValue)
    }

    "resolve properly null values when converter does not handle null properly" in new Context {
      override val resolver = new StringResolver {
        override def defaultResolution(request: HttpServletRequest): String = null
        override val filterParam: FilterParameters.Value = aFilterParam
        override def convert(value: String): String = {
          if (value == null) throw new RuntimeException("cannot handle null value") else value
        }
      }

      resolver.resolve(request, emptyConfig, emptyCustomConverters) must not(throwA[Exception])
    }
  }
}