package com.wixpress.guineapig.spring

import java.util.Properties

import org.apache.velocity.app.VelocityEngine

import scala.collection.JavaConversions._

case class VelocityEngineBuilder(velocityFileSystemLocations: Map[String, String] = Map.empty,
                                 velocityClasspathLocations: Seq[String] = Seq.empty,
                                 useCache: Boolean = true,
                                 strictReferences: Boolean = false,
                                 extraProperties: Map[String, AnyRef] = Map.empty) {

  def build(): VelocityEngine = {
    val properties = new Properties()
    properties.putAll(extraProperties)
    val velocityEngine = new VelocityEngine(properties)
    velocityEngine.init()
    velocityEngine
  }

}

object VelocityEngineBuilder {
  private val NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange = "0"
}