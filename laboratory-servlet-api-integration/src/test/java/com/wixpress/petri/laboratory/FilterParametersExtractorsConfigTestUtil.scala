package com.wixpress.petri.laboratory

object FilterParametersExtractorsConfigTestUtil {
  def forParamOptionAndName(param: FilterParameters.Value, option: HttpRequestExtractionOptions.Value,
                            name: String): FilterParametersExtractorsConfig =
    new FilterParametersExtractorsConfig(Map(param.toString -> List((option.toString, name))))
}
