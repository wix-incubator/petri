package com.wixpress.guineapig.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.MakeItEasy;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.guineapig.dsl.ExperimentBuilders;
import com.wixpress.guineapig.entities.ui.ExperimentConverter;
import com.wixpress.guineapig.entities.ui.ExperimentReport;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.spi.HardCodedScopesProvider;
import com.wixpress.guineapig.util.MockHardCodedScopesProvider;
import com.wixpress.guineapig.util.ReportMatchers;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.petri.Clock;
import com.wixpress.petri.petri.ConductExperimentSummary;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.SpecDefinition;
import org.hamcrest.CoreMatchers;
import org.jmock.AbstractExpectations;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.wixpress.guineapig.util.Matchers.isForAction;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.empty;

public class ExperimentMgmtServiceTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final FullPetriClient fullPetriClient = context.mock(FullPetriClient.class);
    private final Clock clock = context.mock(Clock.class);
    private final EventPublisher experimentEventPublisher = context.mock(EventPublisher.class);
    private final SpecService specService = context.mock(SpecService.class);
    private final HardCodedScopesProvider hardCodedScopesProvider = new MockHardCodedScopesProvider();
    private final ExperimentMgmtService ems = new ExperimentMgmtService(clock, experimentEventPublisher, fullPetriClient, hardCodedScopesProvider);
    private final String someKey = "someKey";

    private final DateTime now = new DateTime();
    private String userName = "";
    private Trigger trigger = new Trigger("", userName);

    @Before
    public void setup() {
        assumingTimeSourceReturnsNow();
    }

    @Test
    public void returnsEmptyListWhenPetriClientHasNoExperiments() throws Exception {
        assumingPetriClientContains(new ArrayList<>());
        Assert.assertThat(ems.getAllExperiments(), CoreMatchers.is(empty()));
    }

    @Test
    public void convertsExperimentsIntoUIExperiments() throws Exception {
        Experiment experiment = MakeItEasy.an(ExperimentMakers.Experiment, MakeItEasy.with(ExperimentMakers.id, 1), MakeItEasy.with(ExperimentMakers.key, "spec1")).make();
        assumingPetriClientContains(asList(experiment));
        Assert.assertThat(ems.getAllExperiments(), CoreMatchers.is(asList(experiment)));
    }

    @Test
    public void returnsCorrectExperimentById() throws Exception {
        Experiment experiment1 = MakeItEasy.an(ExperimentMakers.Experiment, MakeItEasy.with(ExperimentMakers.id, 1), MakeItEasy.with(ExperimentMakers.key, "spec1")).make();
        Experiment experiment2 = MakeItEasy.an(ExperimentMakers.Experiment, MakeItEasy.with(ExperimentMakers.id, 2), MakeItEasy.with(ExperimentMakers.key, "spec2")).make();
        assumingPetriClientContains(asList(experiment1, experiment2));
        Assert.assertThat(ems.getExperimentById(2), CoreMatchers.is(experiment2));
    }

    @Test
    public void returnsCorrectExperimentsReportById() throws Exception {
        ConductExperimentSummary summary = new ConductExperimentSummary("localhost", 2, "true", 1L, 1L, new DateTime());
        assumingPetriClientContains(ImmutableList.of(summary));
        ExperimentReport experimentReport = ems.getExperimentReport(summary.experimentId());
        Assert.assertThat(experimentReport, ReportMatchers.hasOneCountForValue("true"));
    }

    @Test
    public void returnsDealerNonEditableExperimentById() throws Exception {
        Experiment experimentOnDealerScope = ExperimentBuilders.createActiveOnNonEditableScope().but(
                MakeItEasy.with(ExperimentMakers.id, 1),
                MakeItEasy.with(ExperimentMakers.key, "anyKey"),
                MakeItEasy.with(ExperimentMakers.fromSpec, false),
                MakeItEasy.with(ExperimentMakers.scope, NOT_EDITABLE_SCOPE))
                .make();
        assumingPetriClientContains(asList(experimentOnDealerScope));
        UiExperiment uiExperiment = convertToUiExperiment(ems.getExperimentById(1));
        Assert.assertThat(uiExperiment.isEditable(), CoreMatchers.is(false));
    }

    @Test
    public void terminateExperiment() {

        final Experiment futureExperiment = ExperimentBuilders.createFuture().but(
                MakeItEasy.with(ExperimentMakers.id, 1))
                .make();

        Assert.assertThat(terminateExperimentWithSpecActive(futureExperiment, true), CoreMatchers.is(true));
    }

    @Test
    public void terminateNonOriginalExperimentTerminatesPausedOriginalToo() throws IOException, ClassNotFoundException {
        final Maker<Experiment> experiment = ExperimentBuilders.createActiveOnNonEditableScope().but(MakeItEasy.with(ExperimentMakers.id, 1), MakeItEasy.with(ExperimentMakers.originalId, 1),
                MakeItEasy.with(ExperimentMakers.paused, true));
        final Experiment updatedExperiment = experiment.but(MakeItEasy.with(ExperimentMakers.description, "blah")).make();

        context.checking(new Expectations() {{
            allowing(fullPetriClient).fetchExperimentById(updatedExperiment.getId());
            will(AbstractExpectations.returnValue(updatedExperiment));

            allowing(fullPetriClient).fetchAllExperiments();
            will(AbstractExpectations.returnValue(asList(updatedExperiment, experiment.make())));
        }});


        context.checking(new Expectations() {{
            allowing(fullPetriClient).updateExperiment(with(updatedExperiment.terminateAsOf(clock.getCurrentDateTime(), trigger)));
            will(AbstractExpectations.returnValue(updatedExperiment));
            oneOf(fullPetriClient).updateExperiment(with(experiment.make().terminateAsOf(clock.getCurrentDateTime(), trigger)));
            will(AbstractExpectations.returnValue(experiment.make()));

            allowing(specService).isSpecActive("", ImmutableList.of());
            will(AbstractExpectations.returnValue(true));

            oneOf(experimentEventPublisher).publish(with(isForAction(ExperimentEvent.TERMINATED)));
        }});

        ems.terminateExperiment(updatedExperiment.getId(), "", userName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void recognizesInvalidFiltersForScope() throws IOException {

        final ScopeDefinition someScope = ScopeDefinition.aScopeDefinitionForAllUserTypes("someScope");

        final SpecDefinition.ExperimentSpecBuilder someSpec = SpecDefinition.ExperimentSpecBuilder.
                aNewlyGeneratedExperimentSpec(someKey).withScopes(someScope);

        final ExperimentSnapshot experimentSnapshot = anExperimentSnapshot().
                withScopes(ImmutableList.of(someScope.getName())).
                withKey(someKey).
                withOnlyForLoggedInUsers(false).
                withGroups(TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING).
                withFeatureToggle(false).
                withStartDate(now).
                withEndDate(now.plusMinutes(5)).
                withFilters(asList(new WixEmployeesFilter())).
                build();


        context.checking(new Expectations() {{
            allowing(fullPetriClient).fetchSpecs();
            will(AbstractExpectations.returnValue(asList(someSpec.build())));
        }});

        ems.newExperiment(experimentSnapshot);
    }

    private boolean terminateExperimentWithSpecActive(final Experiment futureExperiment, final boolean isSpecActive) {
        context.checking(new Expectations() {{
            allowing(fullPetriClient).fetchAllExperiments();
            will(AbstractExpectations.returnValue(asList(futureExperiment)));

            allowing(fullPetriClient).fetchExperimentById(futureExperiment.getId());
            will(AbstractExpectations.returnValue(futureExperiment));
        }});

        assumingPetriClientContains(asList(futureExperiment));

        context.checking(new Expectations() {{
            allowing(fullPetriClient).updateExperiment(with(futureExperiment.terminateAsOf(
                    clock.getCurrentDateTime(), new Trigger("terminate experiment", userName))));
            will(AbstractExpectations.returnValue(futureExperiment));
            oneOf(experimentEventPublisher).publish(with(isForAction(ExperimentEvent.TERMINATED)));

            allowing(specService).isSpecActive(futureExperiment.getKey(), ImmutableList.of());
            will(AbstractExpectations.returnValue(isSpecActive));
        }});

        return ems.terminateExperiment(futureExperiment.getId(), "terminate experiment", userName);
    }

    private void assumingTimeSourceReturnsNow() {
        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(AbstractExpectations.returnValue(now));
        }});
    }

    private void assumingPetriClientContains(final List<Experiment> contents) {
        context.checking(new Expectations() {{
            allowing(fullPetriClient).fetchAllExperimentsGroupedByOriginalId();
            will(AbstractExpectations.returnValue(contents));
        }});
    }

    private void assumingPetriClientContains(final ImmutableList<ConductExperimentSummary> reports) {
        context.checking(new Expectations() {{
            allowing(fullPetriClient).getExperimentReport(2);
            will(AbstractExpectations.returnValue(reports));
        }});
    }

    private UiExperiment convertToUiExperiment(Experiment experiment) throws JsonProcessingException, ClassNotFoundException {
        ExperimentConverter converter = new ExperimentConverter(new IsEditablePredicate(), new NoOpFilterAdapterExtender());
        return converter.convert(experiment);
    }

    public static final String NOT_EDITABLE_SCOPE = "NOT_EDITABLE_SCOPE";
    public class IsEditablePredicate implements Predicate<Experiment> {
        @Override
        public boolean apply(@Nullable Experiment experiment) {
            return !NOT_EDITABLE_SCOPE.equals(experiment.getScope());
        }
    }
}
