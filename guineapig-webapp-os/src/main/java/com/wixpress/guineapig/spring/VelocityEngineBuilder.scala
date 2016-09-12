package com.wixpress.guineapig.spring

import java.util.{Collections, Properties}

import com.wixpress.guineapig.velocity.FileResourceLoader
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.ResourceManagerImpl

import scala.collection.JavaConversions._

case class VelocityEngineBuilder(velocityFileSystemLocations: Map[String, String] = Map.empty,
                                 velocityClasspathLocations: Seq[String] = Seq.empty,
                                 useCache: Boolean = true,
                                 strictReferences: Boolean = false,
                                 extraProperties: Map[String, AnyRef] = Map.empty) {

  import VelocityEngineBuilder._

  def build(): VelocityEngine = {
    val properties = new Properties()
    properties.putAll(baseProperties ++ extraProperties)
    val velocityEngine = new VelocityEngine(properties)
    velocityEngine.init()
    velocityEngine
  }

  private def baseProperties = Map[String, AnyRef](
    "input.encoding" -> "UTF-8",
    "output.encoding" -> "UTF-8",
    "resource.loader" -> "classpath, file",
    "resource.manager.defaultcache.size" -> new Integer(200),
    "resource.manager.class" -> classOf[ResourceManagerImpl].getName,
    "classpath.resource.loader.class" -> classOf[FileResourceLoader].getName,
    "classpath.resource.loader.path" -> "/",
    "classpath.resource.loader.paths" -> Collections.emptyMap(),
    "classpath.resource.loader.modificationCheckInterval" ->
      NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange,
    "classpath.resource.loader.cache" -> java.lang.Boolean.valueOf(useCache),
    "classpath.resource.loader.prefix" -> velocityClasspathLocations,
    "file.resource.loader.instance" -> new FileResourceLoader, // todo guineapig-os: this might require attention. The original ResourceLoader passed in the wix version was the framework's PathMapFileResourceLoader
    "file.resource.loader.paths" -> Collections.emptyMap(),
    "file.resource.loader.modificationCheckInterval" ->
      NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange,
    "file.resource.loader.cache" -> java.lang.Boolean.valueOf(useCache),
//    "velocimacro.library" -> "org/springframework/web/servlet/view/velocity/spring.vm",
    "eventhandler.referenceinsertion.class" -> "org.apache.velocity.app.event.implement.EscapeJavaScriptReference",
    "eventhandler.include.class" -> "org.apache.velocity.app.event.implement.IncludeRelativePath",
    "eventhandler.escape.javascript.match" -> "/unsafeJS.*/",
    "runtime.references.strict" -> java.lang.Boolean.valueOf(strictReferences)
  )
}

object VelocityEngineBuilder {
  private val NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange = "0"
}