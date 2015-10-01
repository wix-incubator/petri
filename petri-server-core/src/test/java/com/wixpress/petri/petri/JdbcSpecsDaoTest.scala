package com.wixpress.petri.petri

import java.util.Arrays._

import com.fasterxml.jackson.databind.JsonMappingException
import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.domain.ExperimentSpec
import com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder._
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.SpecWithJUnit

/**
 * @author dmitryk
 * @since 17-Sep-2015
 */
class JdbcSpecsDaoTest extends SpecWithJUnit with JMock {

  "JdbcScalaSpecsDao" should {

    "throw on serialization error" in {
      val spec = anExperimentSpec("f.q.n", DateTime.now(DateTimeZone.UTC)).withTestGroups(asList("on", "off")).build

      val mockMapper = mock[PetriMapper[ExperimentSpec]]
      checking {
        oneOf(mockMapper).serialize(spec) willThrow new JsonMappingException("")
      }

      val dao = new JdbcSpecsDao(jdbcTemplate = null, mapper = mockMapper)

      dao.add(spec) must throwA[FullPetriClient.PetriException]
    }

  }

}
