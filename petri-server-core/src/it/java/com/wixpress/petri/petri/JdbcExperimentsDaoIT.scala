package com.wixpress.petri.petri

import java.io.IOException
import java.util.Arrays.asList

import com.natpryce.makeiteasy.MakeItEasy.{`with` => withA, _}
import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot, Trigger}
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import com.wixpress.petri.laboratory.dsl.ExperimentMakers
import com.wixpress.petri.laboratory.dsl.ExperimentMakers._
import com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.specs2.mutable.{Before, SpecWithJUnit}

/**
 * @author dmitryk
 * @since 17-Sep-2015
 */
class JdbcExperimentsDaoIT extends SpecWithJUnit with JMock {

  sequential

  "JdbcScalaExperimentsDao" should {

    "fetch all should return empty list" in new ctx {
      dao.fetch() must beEmpty
    }

    "add new experiment from spec" in new ctx {
      val given = givenExperimentWithSpec(snapshot)
      given.getId must be_==(1)
      given.getExperimentSnapshot must be_==(snapshot)
      dbDriver.getExperimentCountByOrigId(1) must be_==(1)
      dbDriver.getExperimentStartEndDates(1) must be_==(List(new Interval(snapshot.startDate, snapshot.endDate)))
    }

    "add new experiment without spec" in new ctx {
      dao.add(experiment.but(withA(fromSpec, Boolean.box(false))).make.getExperimentSnapshot)
    }

    "throw exception on non existing experiment" in new ctx {
      dao.add(snapshot) must throwA[FullPetriClient.CreateFailed]
    }

    "update creates new row" in new ctx {
      givenExperimentWithSpec(snapshot)

      val afterInsert = experiment.but(withA(id, Int.box(1)))
      dao.fetch().head must be_==(afterInsert.make)

      val toUpdate = afterInsert.but(withA(description, "new desc")).make

      dao.update(toUpdate, now)

      dao.fetch().head must be_==(afterInsert.but(withA(description, "new desc"), withA(lastUpdated, now)).make)
    }

    "update" in new ctx {
      val experimentWithExistingOriginalId = experiment.but(withA(originalId, Int.box(1)))
      val persistedExperiment = givenExperimentWithSpec(experimentWithExistingOriginalId.make.getExperimentSnapshot)
      val mutatedExperimentMaker = experimentWithExistingOriginalId.but(withA(id, Int.box(persistedExperiment.getId)), withA(description, "bla"))
      val mutatedExperiment = mutatedExperimentMaker.make
      val updateTime = now.plusSeconds(1)
      dao.update(mutatedExperiment, updateTime)
      val expectedMutatedExperiment = mutatedExperimentMaker.but(withA(lastUpdated, updateTime)).make
      dao.fetch() must be_==(Seq(expectedMutatedExperiment))
      //assert orig id is propagated
      dbDriver.getExperimentCountByIdAndOrigId(1, 1) must be_==(2)
      //TODO - perhaps replace with better assert once history feature is implemented
      dao.getHistoryById(persistedExperiment.getId) must be_==(List(expectedMutatedExperiment, persistedExperiment))

      //verify updated experiment can be terminated (bug found by chance only in E2E)
      dao.update(expectedMutatedExperiment.terminateAsOf(nowTime, new Trigger("", "")), updateTime.plusSeconds(10))
    }

    "update not from spec" in new ctx {
      val experimentSnapshotNotFromSpec = experiment.but(withA(fromSpec, Boolean.box(false)), withA(ExperimentMakers.key, "NON_EXISTING_KEY"))
      val persistedExperiment = dao.add(experimentSnapshotNotFromSpec.make.getExperimentSnapshot)
      val mutatedExperiment = experimentSnapshotNotFromSpec.but(withA(id, Int.box(persistedExperiment.getId))).make
      dao.update(mutatedExperiment, nowTime)
    }

    "update startDate/endDate" in new ctx {
      val experiment1 = givenExperimentWithSpec(snapshot)

      val experiment2 = experiment.but(
        withA(id, Int.box(experiment1.getId)),
        withA(startDate, experiment1.getStartDate.plusDays(1)),
        withA(endDate, experiment1.getEndDate.plusDays(2))
      ).make()

      dao.update(experiment2, now.plusSeconds(1))

      dbDriver.getExperimentStartEndDates(experiment1.getOriginalId) must be_==(List(
        new Interval(experiment1.getStartDate, experiment1.getEndDate),
        new Interval(experiment2.getStartDate, experiment2.getEndDate)
      ))
    }

    "throw for non existent experiment update" in new ctx {
      givenExperimentWithSpec(snapshot)

      dao.update(experiment.but(withA(id, Int.box(3))).make, nowTime) must throwA[FullPetriClient.UpdateFailed]
    }

    "throw for non existent key update" in new ctx {
      val persistedExperiment = givenExperimentWithSpec(snapshot)
      val mutatedExperiment = experiment.but(withA(ExperimentMakers.key, "NON_EXISTING_KEY"), withA(id, Int.box(persistedExperiment.getId))).make
      dao.update(mutatedExperiment, nowTime) must throwA[FullPetriClient.UpdateFailed]
    }

    "only the latest original id is fetched" in new ctx {
      val persistedExperiment = givenExperimentWithSpec(snapshot)
      val experimentWithSameOriginalId = experiment.but(withA(originalId, Int.box(persistedExperiment.getId)))
      dao.add(experimentWithSameOriginalId.make.getExperimentSnapshot)
      dao.fetchAllExperimentsGroupedByOriginalId.map(e => (e.getId, e.getOriginalId)) must be_==(List((2, 1)))
    }

    "history contains all original ids" in new ctx {
      val given = givenExperimentWithSpec(snapshot)
      val experimentWithSameOriginalId = experiment.but(withA(originalId, Int.box(given.getId)), withA(creationDate, now.plusSeconds(5)))
      val persistedWithCopiedOrigId = dao.add(experimentWithSameOriginalId.make.getExperimentSnapshot)
      dao.getHistoryById(persistedWithCopiedOrigId.getId) must be_==(List(persistedWithCopiedOrigId, given))
    }

    "throw when updating stale version" in new ctx {
      val persistedExperiment = givenExperimentWithSpec(snapshot)
      dao.update(persistedExperiment, now)
      dao.update(persistedExperiment, now) must throwA[FullPetriClient.UpdateFailed]
    }

    "return null on non-deserializable experiment" in new ctx {
      checking {
        oneOf(mappingErrorHandler).handleError(`with`(contain("illegal")), `with`(contain("experiment")), `with`(any[IOException]))
      }

      dbDriver.insertIllegalExperiment()

      dao.fetch() must be_==(Seq(null))
    }

    "fetchByLastUpdate returns empty" in new ctx {
      val date = givenExperimentWithSpec(snapshot).getLastUpdated
      dao.fetchByLastUpdate(date.minusMinutes(3), date.minusMinutes(1)) must beEmpty
      dao.fetchByLastUpdate(date.plusMinutes(1), date.plusMinutes(3)) must beEmpty
    }

    "fetchByLastUpdate returns the only version" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchByLastUpdate(exp.getLastUpdated.minusMinutes(1), exp.getLastUpdated.plusMinutes(1)) must be_==(List(exp))
    }

    "fetchByLastUpdate returns the latest" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      val expUpdate = experiment.but(
        withA(id, Int.box(exp.getId)),
        withA(originalId, Int.box(exp.getOriginalId)),
        withA(description, "new description"))
      dao.update(expUpdate.make, exp.getLastUpdated.plusMinutes(1))

      val list = dao.fetchByLastUpdate(exp.getLastUpdated.minusMinutes(30), exp.getLastUpdated.plusMinutes(30))
      list must haveSize(1)
      list.head.getDescription must be_==("new description")
    }

    "fetchByLastUpdate returns the latest within interval even if newest exists" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      val expUpdate = experiment.but(
        withA(id, Int.box(exp.getId)),
        withA(originalId, Int.box(exp.getOriginalId)),
        withA(description, "new description"))
      dao.update(expUpdate.make, exp.getLastUpdated.plusMinutes(35))

      dao.fetchByLastUpdate(exp.getLastUpdated.minusMinutes(30), exp.getLastUpdated.plusMinutes(30)) must be_==(List(exp))
    }
  }

  "fetchBetweenStartEndDates" should {
    "return nothing before startDate" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchBetweenStartEndDates(exp.getStartDate.minusMillis(1)) must beEmpty
    }

    "return experiment on startDate" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchBetweenStartEndDates(exp.getStartDate) must be_==(List(exp))
    }

    "return experiment in the middle of interval" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      val midStartEndDate = new DateTime((exp.getStartDate.getMillis + exp.getEndDate.getMillis) / 2, DateTimeZone.UTC)
      dao.fetchBetweenStartEndDates(midStartEndDate) must be_==(List(exp))
    }

    "return experiment before endDate" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchBetweenStartEndDates(exp.getEndDate.minusMillis(1)) must be_==(List(exp))
    }

    "return nothing on endDate" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchBetweenStartEndDates(exp.getEndDate) must beEmpty
    }

    "return the latest experiment entry" in new ctx {
      val maker = experiment.but(withA(startDate, now.minusDays(1)), withA(endDate, now.plusDays(1)), withA(description, "desc1"))

      val activeExperiment = givenExperimentWithSpec(maker.make.getExperimentSnapshot)
      dao.fetchBetweenStartEndDates(now.plusMinutes(1)).map(_.getDescription) must be_==(List("desc1"))

      val updatedActiveExperiment = an(copyOf(activeExperiment), withA(description, "desc2")).make
      dao.update(updatedActiveExperiment, now.plusSeconds(1))
      dao.fetchBetweenStartEndDates(now.plusMinutes(1)).map(_.getDescription) must be_==(List("desc2"))

      val experimentUpdatedToBeTerminated = an(copyOf(updatedActiveExperiment), withA(startDate, now.minusDays(1)), withA(endDate, now.minusMinutes(1)),
        withA(lastUpdated, now.plusSeconds(1)), withA(description, "desc3")).make
      dao.update(experimentUpdatedToBeTerminated, now.plusSeconds(2))
      dao.fetchBetweenStartEndDates(now.plusMinutes(1)) must beEmpty
    }
  }

  "fetchEndingBetween" should {
    "return nothing when no ended experiments exist in interval" in new ctx {
      givenExperimentWithSpec(snapshot)
      dao.fetchEndingBetween(now.minusDays(3), now) must be_==(List())
    }

    "return experiment that has ended in the given interval" in new ctx {
      givenExperimentWithSpec(snapshot)
      val endedExpSnapshot = snapshot.copy(
        key = "endingInsideInterval",
        creationDate = now.minusMonths(1),
        startDate = now.minusDays(5),
        endDate = now.minusMinutes(1))
      val endedExperiment = givenExperimentWithSpec(endedExpSnapshot)
      dao.fetchEndingBetween(now.minusMinutes(5), now) must be_==(List(endedExperiment))
    }

    "regard only last version of id" in new ctx {
      val endingExpSnapshotInsideIntervalMaker = experiment.but(
        withA(ExperimentMakers.key, "endingInsideInterval"),
        withA(creationDate, now.minusMonths(1)),
        withA(startDate, now.minusDays(5)),
        withA(endDate, now.minusMinutes(1)));
      private val endingExpSnapshotInsideInterval = givenExperimentWithSpec(endingExpSnapshotInsideIntervalMaker.make().getExperimentSnapshot)

      val editedToHaveEndOutsideInterval = endingExpSnapshotInsideInterval.terminateAsOf(now.minusMinutes(20), new Trigger("terminated out of interval",""))
      dao.update(editedToHaveEndOutsideInterval, now.minusMinutes(20));

      dao.fetchEndingBetween(now.minusMinutes(10), now) must beEmpty
    }
  }

  "fetchExperimentById" should {

    "return Some(None) if experiment does not exist" in new ctx {
      dao.fetchExperimentById(1337) must beNone
    }

    "return the only experiment if one exists" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      dao.fetchExperimentById(exp.getOriginalId) must beSome(exp)
    }

    "return the updated experiment if it was updated" in new ctx {
      val exp = givenExperimentWithSpec(snapshot)
      val expUpdate = experiment.but(
        withA(id, Int.box(exp.getId)),
        withA(originalId, Int.box(exp.getOriginalId)),
        withA(description, "new description"))
      val expected = expUpdate.but(withA(lastUpdated, exp.getLastUpdated.plusMinutes(35))).make

      dao.update(expUpdate.make, expected.getLastUpdated)
      dao.fetchExperimentById(exp.getId) must beSome(expected)
    }
  }

  "migrateStartEndDates" should {
    "succeed" in new migrationCtx {
      /**
       * Legend:
       * exp1 - created old way, updated new way
       * exp2 - created old way, updated old way
       * exp3 - created old way
       * exp4 - created new way
       * exp5 - broken experiment (non parsable)
       */

      checking {
        allowing(mappingErrorHandler).handleError(`with`(any), `with`(any), `with`(any))
      }

      val exp1v1 = createExperimentOldWay(1, 2)
      dbDriver.getExperimentRows.map(e => (e.startDate, e.endDate)) must be_==(List(defaultInterval))

      val exp1v2 = updateExperimentNewWay(exp1v1, 1)

      dbDriver.getExperimentRows.map(e => (e.startDate, e.endDate)) must be_==(List(defaultInterval, getInterval(exp1v2)))

      val exp2v1 = createExperimentOldWay(2, 10)
      val exp2v2 = updateExperimentOldWay(exp2v1, 5)

      val exp3 = createExperimentOldWay(3, 3)
      val exp4 = createExperimentNewWay(4, 4)

      dbDriver.insertIllegalExperiment()

      dbDriver.getExperimentRows.map(e => (e.id, e.lastUpdateDate, (e.startDate, e.endDate))) must be_==(List(
        (1, 0L, defaultInterval),
        (exp1v1.getId, exp1v1.getLastUpdated.getMillis, defaultInterval),
        (exp1v2.getId, exp1v2.getLastUpdated.getMillis, getInterval(exp1v2)),
        (exp2v1.getId, exp2v1.getLastUpdated.getMillis, defaultInterval),
        (exp2v2.getId, exp2v2.getLastUpdated.getMillis, defaultInterval),
        (exp3.getId, exp3.getLastUpdated.getMillis, defaultInterval),
        (exp4.getId, exp4.getLastUpdated.getMillis, getInterval(exp4))
      ))

      dao.migrateStartEndDates() must be_==(4)

      dbDriver.getExperimentRows.map(e => (e.id, e.lastUpdateDate, (e.startDate, e.endDate))) must be_==(List(
        (1, 0L, defaultInterval),
        (exp1v1.getId, exp1v1.getLastUpdated.getMillis, getInterval(exp1v1)),
        (exp1v2.getId, exp1v2.getLastUpdated.getMillis, getInterval(exp1v2)),
        (exp2v1.getId, exp2v1.getLastUpdated.getMillis, getInterval(exp2v1)),
        (exp2v2.getId, exp2v2.getLastUpdated.getMillis, getInterval(exp2v2)),
        (exp3.getId, exp3.getLastUpdated.getMillis, getInterval(exp3)),
        (exp4.getId, exp4.getLastUpdated.getMillis, getInterval(exp4))
      ))
    }
  }

  private def nowTime = DateTime.now(DateTimeZone.UTC)

  trait BaseCtx extends Before {
    val now = nowTime

    private val jdbcConnectionString: String = DBDriver.JDBC_H2_IN_MEM_CONNECTION_STRING
    val dbDriver = DBDriver.dbDriver(jdbcConnectionString)
    dbDriver.createSchema()
    dbDriver.createReadOnlyH2User()
    val objectMapper = ObjectMapperFactory.makeObjectMapper
    val mappingErrorHandler = mock[MappingErrorHandler]
    val jdbcTemplateRW = dbDriver.jdbcTemplate
    val jdbcTemplateRO = dbDriver.getJdbcTemplateRO(jdbcConnectionString)
    val dao = new JdbcExperimentsDao(jdbcTemplateRW, jdbcTemplateRO, new ExperimentMapper(objectMapper, mappingErrorHandler))

    override def before: Any = {
      dbDriver.createSchema()
      dbDriver.createReadOnlyH2User()
    }

    def givenSpec(key: String) = {
      val serialized = objectMapper.writeValueAsString(new ExperimentSpecBuilder(key, now).withTestGroups(asList("1", "2")).build)
      dbDriver.insertSpec(serialized, key)
    }

    def givenExperimentWithSpec(snapshot: ExperimentSnapshot): Experiment = {
      givenSpec(snapshot.key)
      dao.add(snapshot)
    }
  }

  trait ctx extends BaseCtx {
    val experimentKey = "ex1"
    val experiment = an(ExperimentMakers.Experiment, withA(ExperimentMakers.key, experimentKey), withA(description, "desc"))
    val snapshot = experiment.make.getExperimentSnapshot
  }

  trait migrationCtx extends BaseCtx {
    val defaultInterval = (0L, 4102444800000L)

    private def createExperimentMaker(experimentId: Int, timeSpan: Int) = an(
      ExperimentMakers.Experiment,
      withA(id, Int.box(experimentId)),
      withA(originalId, Int.box(experimentId)),
      withA(creationDate, now.minusDays(timeSpan)),
      withA(startDate, now.minusDays(timeSpan)),
      withA(endDate, now.plusDays(timeSpan)),
      withA(ExperimentMakers.key, s"specs.Experiment_$experimentId"),
      withA(description, s"desc_$experimentId")
    )

    def createExperimentOldWay(experimentId: Int, timeSpan: Int): Experiment = {
      val exp = createExperimentMaker(experimentId, timeSpan).make.getExperimentSnapshot

      givenSpec(exp.key)

      dbDriver.insertExperiment(exp)

      fetchById(experimentId)
    }

    def createExperimentNewWay(experimentId: Int, timeSpan: Int): Experiment = {
      val exp = createExperimentMaker(experimentId, timeSpan).make.getExperimentSnapshot
      givenExperimentWithSpec(exp)
      fetchById(experimentId)
    }

    def updateExperimentOldWay(exp: Experiment, newTimeSpan: Int): Experiment = {
      val updatedExperiment = an(
        copyOf(exp),
        withA(creationDate, exp.getLastUpdated.plusMinutes(1)), // this is a hack to set another last_update_date
        withA(startDate, now.minusDays(newTimeSpan)),
        withA(endDate, now.plusDays(newTimeSpan)))

      dbDriver.insertExperiment(updatedExperiment.make.getExperimentSnapshot)

      fetchById(exp.getId)
    }

    def updateExperimentNewWay(exp: Experiment, newTimeSpan: Int): Experiment = {
      val updatedExperiment = an(
        copyOf(exp),
        withA(startDate, now.minusDays(1)),
        withA(endDate, now.plusDays(1)))

      dao.update(updatedExperiment.make, exp.getLastUpdated.plusMinutes(1))

      fetchById(exp.getId)
    }

    def fetchById(id: Int): Experiment = {
      dao.fetch().find(_.getId == id).get
    }

    def getInterval(exp: Experiment) = (exp.getStartDate.getMillis, exp.getEndDate.getMillis)

  }

}
