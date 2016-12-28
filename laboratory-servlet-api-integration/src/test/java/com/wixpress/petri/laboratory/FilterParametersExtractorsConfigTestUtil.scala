package com.wixpress.petri.laboratory

object FilterParametersExtractorsConfigTestUtil {
  def forParamOptionAndName(param: FilterParameters.Value, option: HttpRequestExtractionOptions.Value,
                            name: String): FilterParametersConfig =
    new FilterParametersConfig(Map(param.toString -> List((option.toString, name))), Map.empty)
}
