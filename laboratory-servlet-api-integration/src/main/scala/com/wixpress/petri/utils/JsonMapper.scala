package com.wixpress.petri.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature._
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonMapper {
  def asJson(obj: Any): String = {
    val defaultModules = Seq(new DefaultScalaModule)

    val mapper = new ObjectMapper().registerModules(defaultModules: _*).disable(WRITE_DATES_AS_TIMESTAMPS)
    mapper.writeValueAsString(obj)
  }
}