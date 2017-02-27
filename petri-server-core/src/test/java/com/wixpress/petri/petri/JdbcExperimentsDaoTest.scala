package com.wixpress.petri.petri

import com.fasterxml.jackson.databind.JsonMappingException
import com.natpryce.makeiteasy.MakeItEasy.{`with` => withA, _}
import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.domain.Experiment
import com.wixpress.petri.laboratory.dsl.ExperimentMakers
import com.wixpress.petri.laboratory.dsl.ExperimentMakers._
import org.specs2.mutable.SpecWithJUnit

/**
 * @author dmitryk
 * @since 17-Sep-2015
 */
class JdbcExperimentsDaoTest extends SpecWithJUnit with JMock {

  "JdbcScalaExperimentsDao" should {

    "throw on serialization error" in {
      val snapshot = an(ExperimentMakers.Experiment, withA(ExperimentMakers.key, "ex1"), withA(description, "desc")).make.getExperimentSnapshot

      val mockMapper = mock[PetriMapper[Experiment]]
      checking {
        oneOf(mockMapper).serialize(snapshot) willThrow new JsonMappingException("")
      }

      val dao = new JdbcExperimentsDao(jdbcTemplateRW = null, jdbcTemplateRO = null, mapper = mockMapper)

      dao.add(snapshot) must throwA[FullPetriClient.PetriException]
    }

  }

}
