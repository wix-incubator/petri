package com.wixpress.guineapig.drivers

import java.util.concurrent.atomic.AtomicBoolean

import com.wixpress.embeddedjetty.JettyServer
import com.wixpress.guineapig.util.ITEmbeddedMysql
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import org.specs2.mutable.{Before, SpecificationWithJUnit}

import scala.sys.ShutdownHookThread

trait SpecificationWithEnvSupport extends SpecificationWithJUnit with Before {

  final val httpDriver = new HttpDriver
  final val om = ObjectMapperFactory.makeObjectMapper

  override def before: Any = {
    GlobalEnv.ensureStarted()

    ShutdownHookThread({
      GlobalEnv.ensureStop()
    })
  }
}

object GlobalEnv {
  private final val started = new AtomicBoolean(false)
  private final val embeddedMySql: ITEmbeddedMysql = new ITEmbeddedMysql(3316)
  private final val server = JettyServer()

  def ensureStarted() = {
    if (!started.get()) {
      started.compareAndSet(false, true)

      embeddedMySql.start()
      server.start()
    }
  }

  def ensureStop(): Unit = {
    if (started.get()) {
      embeddedMySql.stop()
      server.stop()

      started.compareAndSet(true, false)
    }

  }
}
