package com.wixpress.petri.laboratory

import java.util.UUID
import javax.servlet.http.HttpServletRequest

import com.wixpress.petri.laboratory.HttpRequestExtractionOptions.{Cookie, Header, Param}

abstract class Resolver[T] {
  val filterParam: FilterParameters.Value

  def resolve(request: HttpServletRequest, filterParametersExtractorsConfig: FilterParametersExtractorsConfig): T = {
    val extractorConfig = filterParametersExtractorsConfig.configs.get(filterParam.toString)
    val filterParamByConfig = extractorConfig.flatMap(_.collectFirst {
      case config if extractBy(request, config).isDefined => extractBy(request, config)
    }).flatten

    filterParamByConfig.getOrElse(defaultResolution(request))
  }

  def defaultResolution(request: HttpServletRequest): T

  def convert(value: String): T

  def extractBy(request: HttpServletRequest, config: (String, String)): Option[T] = {
    val header = Header.toString
    val cookie = Cookie.toString
    val param = Param.toString

    val extractedValue = config match {
      case (`header`, name) => Option(request.getHeader(name))
      case (`cookie`, name) => Option(request.getCookies).flatMap(_.find(_.getName == name).map(_.getValue))
      case (`param`, name) => Option(request.getParameter(name))
      case _ => None
    }

    extractedValue.map(convert)
  }
}

trait StringResolver extends Resolver[String] {
  override def convert(value: String): String = value
}

class CountryResolver extends StringResolver {
  override val filterParam = FilterParameters.Country

  override def defaultResolution(request: HttpServletRequest): String = {
    val countryByDefaultHeader = request.getHeader("GEOIP_COUNTRY_CODE")
    Option(countryByDefaultHeader).getOrElse(request.getLocale.getCountry)
  }
}

object CountryResolver {
  def apply(): CountryResolver = new CountryResolver
}

class LanguageResolver extends StringResolver {
  override val filterParam = FilterParameters.Language

  override def defaultResolution(request: HttpServletRequest): String = {
    request.getLocale.getLanguage
  }
}

object LanguageResolver {
  def apply(): LanguageResolver = new LanguageResolver()
}

class UserIdResolver extends Resolver[UUID] {
  override val filterParam = FilterParameters.UserId

  override def convert(value: String): UUID = UUID.fromString(value)

  override def defaultResolution(request: HttpServletRequest): UUID = {
    val userId: String = request.getParameter("laboratory_user_id")
    if (userId == null) null
    else convert(userId)
  }
}

object UserIdResolver {
  def apply(): UserIdResolver = new UserIdResolver()
}