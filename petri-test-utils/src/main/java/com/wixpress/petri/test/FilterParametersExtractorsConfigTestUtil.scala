package com.wixpress.petri.test

import java.io.{File, FileOutputStream}

import com.wixpress.petri.laboratory.FilterParametersExtractorsConfig

class FilterParametersExtractorsConfigTestUtil {
  def replaceConfig(file: File, config: FilterParametersExtractorsConfig) = {
    FilterParametersExtractorsConfig.yamlObjectMapper.getFactory.createGenerator(new FileOutputStream(file)).writeObject(config)
  }
}
