package com.wixpress.petri.laboratory

object FilterParametersExtractorsConfigTestUtil {
  def forParamOptionAndName(param: FilterParameter, option: HttpRequestExtractionOption, name: String): FilterParametersConfig =
    new FilterParametersConfig(Map(param -> List((option, name))), Map.empty)
}
