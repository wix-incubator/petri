package com.wixpress.petri.laboratory

import java.util.UUID
import javax.servlet.http.HttpServletRequest


trait Converter[T] {
  def convert(value: String): T
}

abstract class Resolver[T >: Null] {
  val filterParam: FilterParameter

  def defaultResolution(request: HttpServletRequest): String

  def convert(value: String): T

  def resolve(request: HttpServletRequest, filterParametersConfig: FilterParametersConfig): T = {
    val extractorConfig = filterParametersConfig.extractors.get(filterParam)

    val extractedByConfig = extractorConfig.flatMap(_.collectFirst {
      case config if extractBy(request, config).isDefined => extractBy(request, config)
    }).flatten

    val extractedValue = extractedByConfig.getOrElse(defaultResolution(request))

    Option(extractedValue).map(convertValue(_, filterParametersConfig.converters)).orNull
  }

  private def extractBy(request: HttpServletRequest, config: (HttpRequestExtractionOption, String)): Option[String] = {

    val extractedValue = config match {
      case (HeaderExtractionOption, name) => Option(request.getHeader(name))
      case (CookieExtractionOption, name) => Option(request.getCookies).flatMap(_.find(_.getName == name).map(_.getValue))
      case (ParamExtractionOption, name) => Option(request.getParameter(name))
      case _ => None
    }
    extractedValue
  }

  private def convertValue(extractedValue: String, converters: Map[FilterParameter, Converter[_]]): T = {

    val converter = converters.get(filterParam).asInstanceOf[Option[Converter[T]]]
    converter.map(_.convert(extractedValue)).getOrElse(convert(extractedValue))
  }
}

trait StringResolver extends Resolver[String] {
  override def convert(value: String): String = value
}

object CountryResolver extends StringResolver {
  override val filterParam = CountryFilterParameter

  override def defaultResolution(request: HttpServletRequest): String =
    Option(request.getHeader("GEOIP_COUNTRY_CODE")).getOrElse(request.getLocale.getCountry)
}

object LanguageResolver extends StringResolver {
  override val filterParam = LanguageFilterParameter

  override def defaultResolution(request: HttpServletRequest): String = request.getLocale.getLanguage
}

object UserIdResolver extends Resolver[UUID] {
  override val filterParam = UserIdFilterParameter

  override def convert(value: String): UUID = {
    if (value == null) null
    else UUID.fromString(value)
  }

  override def defaultResolution(request: HttpServletRequest): String =
    Option(request.getParameter("laboratory_user_id")).orNull
}