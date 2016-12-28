package com.wixpress.common.petri.e2e

import java.io.{File, FileOutputStream}

import com.wixpress.common.petri.SampleUserIdConverter
import com.wixpress.petri.laboratory.{FilterParametersExtractorsConfigReader, FilterParametersExtractorsConfig}
import com.wixpress.petri.test.SampleAppRunner

class FilterParametersExtractorsConfigTestUtil {
  def replaceConfigWithGeoHeaderAndUserIdConverter(file: File) = {
    val geoHeaderConfig = "Country" -> List(("Header", SampleAppRunner.GEO_HEADER))
    val userIdConverterConfig = "UserId" -> List(("Converter", classOf[SampleUserIdConverter].getName))
    val config = FilterParametersExtractorsConfig(Map(geoHeaderConfig, userIdConverterConfig))
    FilterParametersExtractorsConfigReader.yamlObjectMapper.getFactory.createGenerator(new FileOutputStream(file)).writeObject(config)
  }
}
