package com.wixpress.guineapig.web

import javax.servlet.annotation.WebServlet

import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.springframework.web.context.support.XmlWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet

@WebServlet(urlPatterns = Array("/", "api/*", "*.css,*.js,*.png,*.gzip,*.otf,*.eot,*.svg,*.ttf,*.woff,*.html"))
class GuineaPigDispatcherServlet(applicationContext: XmlWebApplicationContext) extends DispatcherServlet(applicationContext)


object GuineaPigDispatcherServlet{

  def addGuineaPigServlet(servletContextHandler: ServletContextHandler) {
    //or this flavor when we move away from xmls
    /*AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
    applicationContext.setConfigLocation("com.wixpress.guineapig.spi");
    applicationContext.register(WebConfig.class);*/

    val applicationContext = new XmlWebApplicationContext
    applicationContext.setConfigLocations("classpath*:spring-servlet.xml", "classpath*:applicationContext.xml")

    val servletHolder = new ServletHolder(new GuineaPigDispatcherServlet(applicationContext))
    servletContextHandler.addServlet(servletHolder, "/")
  }
}