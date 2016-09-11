package com.wixpress.guineapig.spi

import com.wixpress.guineapig.dto.SpecExposureIdViewDto

trait SpecExposureIdRetriever {

  def getAll: java.util.List[SpecExposureIdViewDto]
}