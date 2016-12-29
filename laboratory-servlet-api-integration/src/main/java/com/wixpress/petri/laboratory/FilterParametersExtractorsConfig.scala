package com.wixpress.petri.laboratory

import javax.servlet.ServletContext

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.io.Source._
import scala.util.{Success, Try}


case class FilterParametersExtractorsConfig(configs: Map[String, List[(String, String)]])

object FilterParametersExtractorsConfig {  
  def apply(): FilterParametersExtractorsConfig = new FilterParametersExtractorsConfig(Map.empty)
}


object FilterParametersExtractorsConfigReader {
  
  val CoverterConfigOption = "Converter"
  
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
    val converters = collection.mutable.Map[FilterParameter, Converter[_]]()
    val extractors = collection.mutable.Map[FilterParameter, List[(HttpRequestExtractionOption, String)]]()

    config.configs.foreach {
      case (filterParamName, filterParamConfig) => {
        val filterParam = FilterParameter.asFilterParameter(filterParamName)
        val extractorsConfig = extractorsOnly(filterParamConfig)
        if (extractorsConfig.nonEmpty)
          extractors.put(filterParam, extractorsConfig)

        val converter = customConverter(filterParamConfig)
        if (converter.isDefined)
          converters.put(filterParam, converter.get)
      }
    }
    FilterParametersConfig(extractors.toMap, converters = converters.toMap)
  }


  private def extractorsOnly(extractorConfig: List[(String, String)]): List[(HttpRequestExtractionOption, String)] = {
    extractorConfig.filterNot(_._1 == CoverterConfigOption).map(pair =>
      (HttpRequestExtractionOption.asExtractionOption(pair._1), pair._2)
    )

  }

  private def customConverter(extractorConfig: List[(String, String)]): Option[Converter[_]] = {
    val converterName = extractorConfig.find(_._1 == CoverterConfigOption).map(_._2)
    converterName.map(Class.forName(_).newInstance().asInstanceOf[Converter[_]])
  }
}
