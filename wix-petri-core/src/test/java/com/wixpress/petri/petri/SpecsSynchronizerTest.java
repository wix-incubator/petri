package com.wixpress.petri.petri;


import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;

/**
 * @author sagyr
 * @since 10/3/13
 */
public class SpecsSynchronizerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private PetriClient petriClient;
    private SpecDefinitions specDefinitions;
    private SpecsSynchronizer specsSynchronizer;
    private Clock fakeClock;

    private void petriClientShouldRecieve(final ExperimentSpec... expectedSpecs) {
        context.checking(new Expectations() {{
            oneOf(petriClient).addSpecs(asList(expectedSpecs));
        }});
    }

    private void assumingSpecDefs(final SpecDefinition... specDefs) {
        context.checking(new Expectations() {{
            allowing(specDefinitions).get();
            will(returnValue(asList(specDefs)));
        }});
    }

    public static class TestableSpecDef extends SpecDefinition {

        private final List<String> groups;

        private TestableSpecDef(List<String> groups) {
            this.groups = groups;
        }

        public static TestableSpecDef ofTestGroups(List<String> groups) {
            return new TestableSpecDef(groups);
        }

        @Override
        protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
            return builder.withTestGroups(groups);
        }
    }

    @Before
    public void setUp() throws Exception {
        petriClient = context.mock(PetriClient.class);
        specDefinitions = context.mock(SpecDefinitions.class);

        fakeClock = context.mock(Clock.class);

        context.checking(new Expectations() {{
            DateTime now = new DateTime();
            allowing(fakeClock).getCurrentDateTime();
            will(onConsecutiveCalls(returnValue(now), returnValue(now), returnValue(now.plusMinutes(1))));

        }});

        specsSynchronizer = new SpecsSynchronizer(petriClient, specDefinitions, fakeClock);
    }

    @Test
    public void syncsSingleSpec() {
        List<String> testGroups = asList("1,2");
        assumingSpecDefs(TestableSpecDef.ofTestGroups(testGroups));
        final ExperimentSpec expectedSpec = anExperimentSpec(TestableSpecDef.class.getName(), fakeClock.getCurrentDateTime())
                .withTestGroups(testGroups).build();
        petriClientShouldRecieve(expectedSpec);
        specsSynchronizer.syncSpecs();
    }

    @Test
    public void doesntSyncWhenNoSpecDefsFound() {
        context.checking(new Expectations() {{
            allowing(specDefinitions).get();
            will(returnValue(new ArrayList<SpecDefinition>()));
            oneOf(petriClient).addSpecs(new ArrayList<ExperimentSpec>());
        }});
        specsSynchronizer.syncSpecs();
    }

    @Test
    public void syncsTwoSpecsCreatesTwoWithIdenticalCreationTimes() {
        List<String> testGroups = asList("1,2");
        DateTime now = fakeClock.getCurrentDateTime();
        SpecDefinition.ExperimentSpecBuilder expectedSpecs = anExperimentSpec(TestableSpecDef.class.getName(), now).withTestGroups(testGroups);
        final ExperimentSpec expectedSpec1 = expectedSpecs.build();
        final ExperimentSpec expectedSpec2 = expectedSpecs.build();

        assumingSpecDefs(TestableSpecDef.ofTestGroups(testGroups), TestableSpecDef.ofTestGroups(testGroups));
        petriClientShouldRecieve(expectedSpec1, expectedSpec2);
        specsSynchronizer.syncSpecs();
    }

}
