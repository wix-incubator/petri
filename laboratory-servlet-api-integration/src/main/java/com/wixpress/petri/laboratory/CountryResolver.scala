package com.wixpress.petri.laboratory

import javax.servlet.http.HttpServletRequest

import com.wixpress.petri.laboratory.HttpRequestExtractionOptions.{Cookie, Header, Param}

trait Resolver {
  val filterParam: FilterParameters.Value
}

class CountryResolver extends Resolver {
  override val filterParam = FilterParameters.Country

  def resolve(request: HttpServletRequest, filterParametersExtractorsConfig: FilterParametersExtractorsConfig): String = {
    val countryExtractorConfig = filterParametersExtractorsConfig.configs.get(filterParam.toString)

    def extractBy(config: (String, String)): Option[String] = {
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

    val countryByConfig = countryExtractorConfig.flatMap(_.collectFirst {
        case config if extractBy(config).isDefined => extractBy(config)
      }).flatten

    def defaultBehavior: String = {
      val countryByDefaultHeader = request.getHeader("GEOIP_COUNTRY_CODE")
      Option(countryByDefaultHeader).getOrElse(request.getLocale.getCountry)
    }

    countryByConfig.getOrElse(defaultBehavior)
  }

  def getValue(req: HttpServletRequest, extractor: HttpServletRequest => String): String = {
    extractor(req)
  }
}

object CountryResolver {
  def apply(): CountryResolver = new CountryResolver
}