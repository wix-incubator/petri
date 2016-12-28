package com.wixpress.petri.laboratory


sealed trait HttpRequestExtractionOption {
  def optionName: String
}

case object ParamExtractionOption extends HttpRequestExtractionOption {
  override val optionName = "Param"
}

case object HeaderExtractionOption extends HttpRequestExtractionOption {
  override val optionName = "Header"
}

case object CookieExtractionOption extends HttpRequestExtractionOption {
  override val optionName = "Cookie"
}

object HttpRequestExtractionOption {
  def asExtractionOption(string: String): HttpRequestExtractionOption = string match {
    case "Param" => ParamExtractionOption
    case "Header" => HeaderExtractionOption
    case "Cookie" => CookieExtractionOption
  }
}


sealed trait FilterParameter {
  def paramName: String
}

case object CountryFilterParameter extends FilterParameter {
  override val paramName = "Country"
}

case object LanguageFilterParameter extends FilterParameter {
  override val paramName = "Language"
}

case object UserIdFilterParameter extends FilterParameter {
  override val paramName = "UserId"
}

object FilterParameter {
  def asFilterParameter(string: String) = string match {
    case "Country" => CountryFilterParameter
    case "Language" => LanguageFilterParameter
    case "UserId" => UserIdFilterParameter
  }
}


case class FilterParametersConfig(extractors: Map[FilterParameter, List[(HttpRequestExtractionOption, String)]], converters: Map[FilterParameter, Converter[_]])

object FilterParametersConfig {
  def apply(): FilterParametersConfig = new FilterParametersConfig(Map.empty, Map.empty)
}
