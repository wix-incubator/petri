package com.wixpress.guineapig.spring

import javax.annotation.Resource

import com.wixpress.guineapig.topology.ServerTopology
import org.apache.velocity.app.VelocityEngine
import org.springframework.context.annotation._
import org.springframework.context.support.ReloadableResourceBundleMessageSource

/**
 * User: Dalias
 * Date: 11/18/14
 * Time: 3:18 PM
 */
class VelocityConfig {

  @Resource var serverTopology: ServerTopology = _

  @Bean
  def velocityEngine: VelocityEngine = VelocityEngineBuilder().build()


  @Bean def messageSource(): ReloadableResourceBundleMessageSource = {
    val messageSource: ReloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource
    messageSource.setUseCodeAsDefaultMessage(true)
    messageSource.setDefaultEncoding("UTF-8")
    messageSource.setBasenames("classpath:/messages/messages")
    messageSource
  }
}