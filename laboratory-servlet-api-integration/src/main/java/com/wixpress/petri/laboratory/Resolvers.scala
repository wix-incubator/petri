package com.wixpress.petri.laboratory

import javax.servlet.http.HttpServletRequest

import com.wixpress.petri.laboratory.HttpRequestExtractionOptions.{Cookie, Header, Param}

trait Resolver {
  val filterParam: FilterParameters.Value

  def resolve(request: HttpServletRequest, filterParametersExtractorsConfig: FilterParametersExtractorsConfig): String = {
    val extractorConfig = filterParametersExtractorsConfig.configs.get(filterParam.toString)
    val filterParamByConfig = extractorConfig.flatMap(_.collectFirst {
      case config if extractBy(request, config).isDefined => extractBy(request, config)
    }).flatten

    filterParamByConfig.getOrElse(defaultResolution(request))
  }

  def defaultResolution(request: HttpServletRequest): String

  def extractBy(request: HttpServletRequest, config: (String, String)): Option[String] = {
    val header = Header.toString
    val cookie = Cookie.toString
    val param = Param.toString

    config match {
      case (`header`, name) => Option(request.getHeader(name))
      case (`cookie`, name) => Option(request.getCookies).flatMap(_.find(_.getName == name).map(_.getValue))
      case (`param`, name) => Option(request.getParameter(name))
      case _ => None
    }
  }
}

class CountryResolver extends Resolver {
  override val filterParam = FilterParameters.Country

  override def defaultResolution(request: HttpServletRequest): String = {
    val countryByDefaultHeader = request.getHeader("GEOIP_COUNTRY_CODE")
    Option(countryByDefaultHeader).getOrElse(request.getLocale.getCountry)
  }
}

object CountryResolver {
  def apply(): CountryResolver = new CountryResolver
}

class LanguageResolver extends Resolver {
  override val filterParam = FilterParameters.Language

  override def defaultResolution(request: HttpServletRequest): String = {
    request.getLocale.getLanguage
  }
}

object LanguageResolver {
  def apply(): LanguageResolver = new LanguageResolver()
}