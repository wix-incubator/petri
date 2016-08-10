package com.wixpress.common.petri.e2e

import java.io.{File, FileOutputStream}

import com.wixpress.petri.laboratory.FilterParametersExtractorsConfig
import com.wixpress.petri.test.SampleAppRunner

class FilterParametersExtractorsConfigTestUtil {
  def replaceConfigWithGeoHeader(file: File) = {
    val config = FilterParametersExtractorsConfig(Map("Country" -> List(("Header", SampleAppRunner.GEO_HEADER))))
    FilterParametersExtractorsConfig.yamlObjectMapper.getFactory.createGenerator(new FileOutputStream(file)).writeObject(config)
  }
}
