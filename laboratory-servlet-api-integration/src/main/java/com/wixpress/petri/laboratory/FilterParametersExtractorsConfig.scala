package com.wixpress.petri.laboratory

import javax.servlet.ServletContext

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.io.Source._
import scala.util.{Success, Try}


object HttpRequestExtractionOptions extends Enumeration {
  val Param, Header, Cookie = Value
}

object FilterParameters extends Enumeration {
  val Country = Value
}

case class FilterParametersExtractorsConfig(configs: Map[String, List[(String, String)]])

object FilterParametersExtractorsConfig {
  def apply(): FilterParametersExtractorsConfig = new FilterParametersExtractorsConfig(Map.empty)
  lazy val yamlObjectMapper =  {
    val mapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def readConfig(context: ServletContext): FilterParametersExtractorsConfig = {
    val filtersConfigPath = "/WEB-INF/filters.yaml"
    Try(yamlObjectMapper.readValue(fromInputStream(context.getResourceAsStream(filtersConfigPath)).mkString,
      classOf[FilterParametersExtractorsConfig])) match {
      case Success(conf) => conf
      case _ => FilterParametersExtractorsConfig()
    }
  }
}
