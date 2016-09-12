package com.wixpress.guineapig.dto

import org.joda.time.DateTime

case class SpecExposureIdViewDto(spec: String, exposureId: Option[String], dateCreated: DateTime, dateUpdated: DateTime)
