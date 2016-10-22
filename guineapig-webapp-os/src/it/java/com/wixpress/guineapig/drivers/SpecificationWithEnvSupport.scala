package com.wixpress.guineapig.drivers

import java.util.concurrent.atomic.AtomicBoolean
import com.wixpress.common.petri.testutils.ServerRunner
import com.wixpress.guineapig.util.ITEmbeddedMysql
import com.wixpress.guineapig.web.GuineaPigDispatcherServlet
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import com.wixpress.petri.petri.RAMPetriClient
import org.specs2.mutable.{Before, SpecificationWithJUnit}

import scala.sys.ShutdownHookThread

trait SpecificationWithEnvSupport extends SpecificationWithJUnit with Before {

  final val httpDriver = new HttpDriver
  final val om = ObjectMapperFactory.makeObjectMapper

  override def before: Any = {
    GlobalEnv.ensureStarted()
    GlobalEnv.fullPetriClient.clearAll()

    ShutdownHookThread({
      GlobalEnv.ensureStop()
    })
  }
}

object GlobalEnv {
  private final val started = new AtomicBoolean(false)
  private final val embeddedMySql: ITEmbeddedMysql = new ITEmbeddedMysql(3316)
  private final val server = new ServerRunner(9901, "src/it/webapp")
  final val fullPetriClient = new RAMPetriClient()

  def ensureStarted() = {
    if (!started.get()) {
      started.compareAndSet(false, true)

      embeddedMySql.start()
      GuineaPigDispatcherServlet.addGuineaPigServlet(server.context)
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
