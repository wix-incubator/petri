package com.wixpress.guineapig.embeddedjetty

import java.io.File
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.servlet.DispatcherServlet
import scala.annotation.varargs
import scala.beans.BeanProperty

class WebUiServer {

  import WebUiServer._

  @BeanProperty
  var port: Int = DefaultPort

  @BeanProperty
  var warRoot: File = new File(WarRoot)

  @BeanProperty
  var webXmlFile: File = new File(WebXmlLocation)

  @BeanProperty
  var contextRoot: String = "/"

  private var server: Server = _
  private var context: WebAppContext = _
  private val logger = LoggerFactory.getLogger(classOf[WebUiServer])
  private var additionalResources: Seq[String] = Nil

  def getSpringContext: ApplicationContext = {
    context.getServletHandler.getServlets.map(_.getServletInstance) collectFirst {
      case ds: DispatcherServlet => ds.getWebApplicationContext
    } getOrElse serverError(s"DispatcherServlet not found for webapp at [$webXmlFile]")
  }

  @throws[Exception]
  def stop(): Unit = {
    server.stop()
    server.destroy()
    logger.info("JettyServer stopped")
  }

  @throws[Exception]
  def start(): Unit = {
    logger.info("starting JettyServer")

    validateWebXmlFile()
    validateWarRoot()
    initServer()
    validateServer()

    logger.info("JettyServer started")
  }

  @varargs
  def setAdditionalBaseResources(resources: String*): Unit = {
    additionalResources = resources
  }

  private def initServer(): Unit = {
    server = new Server(port)
    context = new WebAppContext
    context.setDescriptor(webXmlFile.getAbsolutePath)
    context.setContextPath(contextRoot)
    context.setParentLoaderPriority(true)
    context.setBaseResource(baseResources)
    server.setHandler(context)

    server.start()
  }

  private def baseResources = {
    val war = warRoot.getAbsolutePath
    val additional = additionalResources.map(new File(_).getAbsolutePath)
    new ResourceCollection((Seq(war) ++ additional).toArray)
  }

  private def validateServer(): Unit = {
    val servletErrors = context.getServletHandler.getServlets.flatMap(s => Option(s.getUnavailableException)).toSeq
    (Option(context.getUnavailableException) ++ servletErrors) collectFirst {
      case cause => serverError(cause, s"Failed to initialize Web Application\n${cause.getMessage}")
    }
  }

  private def validateWarRoot(): Unit = {
    if (!warRoot.exists()) serverError {
      s"""WAR root directory not found at [${webXmlFile.getAbsolutePath}].
          |Normally it should be at [$WarRoot] in your project
          |Have you set your working directory to the $$MODULE_DIR$$?
       """.stripMargin
    }
  }

  private def validateWebXmlFile(): Unit = {
    if (!(webXmlFile.exists() && webXmlFile.isFile)) serverError {
      s"""web.xml file not found at [${webXmlFile.getAbsolutePath}].
          |Normally it should be at [$WebXmlLocation] in your project.
          |Have you set your working directory to the $$MODULE_DIR$$?
       """.stripMargin
    }
  }

  private def serverError(msg: => String) = throw new JettyServerException(msg)
  private def serverError(cause: Throwable, msg: String) = throw new JettyServerException(msg, cause)
}

object WebUiServer {

  def apply(port: Int): WebUiServer = WebUiServer(port)

  def apply(port: Int = DefaultPort,
            webXmlLocation: String = WebXmlLocation,
            warRoot: String = WarRoot,
            additionalResources: Seq[String] = Nil,
            contextRoot: String = DefaultContextRoot): WebUiServer = {
    try{
      val server = new WebUiServer
      server.port = port
      server.webXmlFile = new File(webXmlLocation)
      server.warRoot = new File(warRoot)
      server.contextRoot = contextRoot
      server.additionalResources = additionalResources
      server
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw e
      }
    }
  }

  final val DefaultPort: Int = 9901

  final val DefaultContextRoot: String = "/"

  final val WebXmlLocation: String = "src/main/webapp/WEB-INF/web.xml"

  final val WarRoot: String = "src/main/webapp"

}

class JettyServerException(msg: String, cause: Throwable) extends RuntimeException(msg, cause) {
  def this(msg: String) = this(msg, null)
}