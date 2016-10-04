package com.wixpress.guineapig.spring

import org.apache.velocity.app.VelocityEngine
import org.springframework.context.annotation._
import org.springframework.context.support.ReloadableResourceBundleMessageSource

/**
 * User: Dalias
 * Date: 11/18/14
 * Time: 3:18 PM
 */
class VelocityConfig {

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