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
  val Country, Language, UserId = Value
}

object FilterParametersConfigOptions extends Enumeration {
  val Converter = Value
}

case class FilterParametersExtractorsConfig(configs: Map[String, List[(String, String)]])

object FilterParametersExtractorsConfig {
  def apply(): FilterParametersExtractorsConfig = new FilterParametersExtractorsConfig(Map.empty)
}

//TODO: change all members to be more type specific
case class FilterParametersConfig(extractors: Map[String, List[(String, String)]], converters: Map[String, Converter[_]])

object FilterParametersConfig {
  def apply(): FilterParametersConfig = new FilterParametersConfig(Map.empty, Map.empty)
}

object FilterParametersExtractorsConfigReader {
  lazy val yamlObjectMapper = {
    val mapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def readConfig(context: ServletContext): FilterParametersConfig = {
    val filtersConfigPath = "/WEB-INF/filters.yaml"
    val extractorsConfig = Try(yamlObjectMapper.readValue(fromInputStream(context.getResourceAsStream(filtersConfigPath)).mkString,
      classOf[FilterParametersExtractorsConfig])) match {
      case Success(conf) => conf
      case _ => FilterParametersExtractorsConfig()
    }
    buildFullConfig(extractorsConfig)
  }

  def buildFullConfig(config: FilterParametersExtractorsConfig): FilterParametersConfig = {
    val converters = collection.mutable.Map[String, Converter[_]]()
    val extractors = collection.mutable.Map[String, List[(String, String)]]()

    config.configs.foreach {
      case (filterParamName, filterParamConfig) => {
        val extractorsConfig = extractorsOnly(filterParamConfig)
        if (extractorsConfig.nonEmpty)
          extractors.put(filterParamName, extractorsConfig)

        val converter = customConverter(filterParamConfig)
        if (converter.isDefined)
          converters.put(filterParamName, converter.get)
      }
    }
    FilterParametersConfig(extractors.toMap, converters = converters.toMap)
  }


  private def extractorsOnly(extractorConfig: List[(String, String)]): List[(String, String)] = {
    extractorConfig.filterNot(_._1 == FilterParametersConfigOptions.Converter.toString)
  }

  private def customConverter(extractorConfig: List[(String, String)]): Option[Converter[_]] = {
    val converterName = extractorConfig.find(_._1 == FilterParametersConfigOptions.Converter.toString).map(_._2)
    converterName.map(Class.forName(_).newInstance().asInstanceOf[Converter[_]])
  }
}
