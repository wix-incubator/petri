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

case class CustomConverters(converters: Map[String, Converter[_]])

object CustomConverters {
  def apply(): CustomConverters = new CustomConverters(Map.empty)
}

object FilterParametersExtractorsConfig {
  def apply(): FilterParametersExtractorsConfig = new FilterParametersExtractorsConfig(Map.empty)
  lazy val yamlObjectMapper = {
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

  def instantiateConverters(config: FilterParametersExtractorsConfig): CustomConverters = {
    val converters = collection.mutable.Map[String, Converter[_]]()

    config.configs.foreach {
      filterParam => {
        val converter = customConverter(filterParam._2)
        if (converter.isDefined)
          converters.put(filterParam._1, converter.get)
      }
    }
    CustomConverters(converters = converters.toMap)
  }

  private def customConverter(extractorConfig: List[(String, String)]): Option[Converter[_]] = {
    val converterName = extractorConfig.find(_._1 == FilterParametersConfigOptions.Converter.toString).map(_._2)
    converterName.map(Class.forName(_).newInstance().asInstanceOf[Converter[_]])
  }
}
