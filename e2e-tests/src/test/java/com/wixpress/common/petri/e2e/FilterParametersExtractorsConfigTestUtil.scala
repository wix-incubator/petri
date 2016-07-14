package com.wixpress.common.petri.e2e

import java.io.{File, FileOutputStream}

import com.wixpress.petri.laboratory.FilterParametersExtractorsConfig

class FilterParametersExtractorsConfigTestUtil {
  def replaceConfig(file: File) = {
    val config = FilterParametersExtractorsConfig(Map("Country" -> List(("Header", "GEO_HEADER"))))
    FilterParametersExtractorsConfig.yamlObjectMapper.getFactory.createGenerator(new FileOutputStream(file)).writeObject(config)
  }
}
