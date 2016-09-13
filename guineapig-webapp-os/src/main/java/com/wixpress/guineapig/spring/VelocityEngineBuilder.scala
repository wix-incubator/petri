package com.wixpress.guineapig.spring

import java.util.{Collections, Properties}
import com.wixpress.guineapig.velocity.WixResourceManager
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
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
    "resource.loader" -> "classpath",
    "resource.manager.defaultcache.size" -> new Integer(200),
    "resource.manager.class" -> classOf[WixResourceManager].getName,
    "classpath.resource.loader.class" -> classOf[ClasspathResourceLoader].getName,
    "classpath.resource.loader.path" -> "/statics/",
    "classpath.resource.loader.paths" -> Collections.emptyMap(),
    "classpath.resource.loader.modificationCheckInterval" ->
      NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange,
    "classpath.resource.loader.cache" -> java.lang.Boolean.valueOf(useCache),
    "classpath.resource.loader.prefix" -> velocityClasspathLocations,
    "eventhandler.referenceinsertion.class" -> "org.apache.velocity.app.event.implement.EscapeJavaScriptReference",
    "eventhandler.include.class" -> "org.apache.velocity.app.event.implement.IncludeRelativePath",
    "eventhandler.escape.javascript.match" -> "/unsafeJS.*/",
    "runtime.references.strict" -> java.lang.Boolean.valueOf(strictReferences)
  )
}

object VelocityEngineBuilder {
  private val NoModificationIntervalSinceWeAlwaysRestartWhenTemplatesChange = "0"
}