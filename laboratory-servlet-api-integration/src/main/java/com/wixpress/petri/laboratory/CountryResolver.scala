package com.wixpress.petri.laboratory

import javax.servlet.http.HttpServletRequest

object CountryResolver {

  def resolve(request: HttpServletRequest, requestDataSourcesConfiguration: FilterParametersExtractorsConfig): String = {
    val headerName = requestDataSourcesConfiguration.configs
      .get(FilterParameters.Country.toString)
      .map(_.head._2)
      .getOrElse("GEOIP_COUNTRY_CODE")

    val geoCountry: String = request.getHeader(headerName)
    if (geoCountry != null) {
      geoCountry
    } else
    request.getLocale.getCountry
  }
}
