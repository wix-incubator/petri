package com.wixpress.petri.petri

import java.io.IOException
import java.util.Arrays._

import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder._
import org.joda.time.DateTime
import org.specs2.mutable.{Before, SpecWithJUnit}
import org.springframework.dao.DuplicateKeyException

import scala.collection.JavaConverters._

/**
 * @author dmitryk
 * @since 21-Sep-2015
 */
class JdbcSpecsDaoIT extends SpecWithJUnit with JMock {

  sequential

  "JdbcScalaSpecsDao" should {

    "return empty list" in new ctx {
      dao.fetch() must beEmpty
    }

    "return null on non-deserializable experiment" in new ctx {
      checking {
        oneOf(mappingErrorHandler).handleError(`with`(contain("illegalSpec")), `with`(contain("experiment")), `with`(any[IOException]))
      }

      dbDriver.insertSpec("illegalSpec", "IRRELEVANT")

      dao.fetch() must be_==(Seq(null))
    }

    "insert" in new ctx {
      val inserted = dao.add(experimentSpec)
      inserted must be_==(experimentSpec)
      dao.fetch() must be_==(Seq(experimentSpec))
    }

    "throw on duplicate key" in new ctx {
      dao.add(experimentSpec)
      dao.add(experimentSpec) must throwA[DuplicateKeyException]
    }

    "update" in new ctx {
      dao.add(experimentSpec)
      dao.update(updatedSpec, nowTime)
      dao.fetch() must be_==(Seq(updatedSpec))
    }

    "delete" in new ctx {
      dao.add(experimentSpec)
      dao.delete(experimentSpec.getKey)
      dao.fetch() must beEmpty
    }

  }

  private def nowTime = DateTime.now()

  trait ctx extends Before {
    val now = nowTime

    val dbDriver = DBDriver.dbDriver(DBDriver.JDBC_H2_IN_MEM_CONNECTION_STRING)
    val objectMapper = ObjectMapperFactory.makeObjectMapper
    val mappingErrorHandler = mock[MappingErrorHandler]
    val dao = new JdbcSpecsDao(dbDriver.jdbcTemplate, new SpecMapper(objectMapper, mappingErrorHandler))

    val experimentSpec = anExperimentSpec("f.q.n", now).withTestGroups(asList("on", "off")).build
    val updatedSpec = anExperimentSpec("f.q.n", now).withTestGroups(asList("a", "b", "c")).build

    override def before: Any = {
      dbDriver.createSchema()
    }
  }

}
