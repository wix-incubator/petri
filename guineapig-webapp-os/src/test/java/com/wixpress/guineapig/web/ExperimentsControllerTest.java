package com.wixpress.guineapig.web;

import com.wixpress.guineapig.dsl.TestGroupMakers;
import com.wixpress.guineapig.entities.ui.ExperimentType;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.services.GuineapigExperimentMgmtService;
import com.wixpress.guineapig.util.MockHardCodedScopesProvider;
import com.wixpress.guineapig.util.MockHardCodedScopesProvider$;
import com.wixpress.guineapig.services.SpecService;
import com.wixpress.guineapig.spi.HardCodedScopesProvider;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.petri.SpecDefinition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

import static com.wixpress.guineapig.entities.ui.UiExperimentBuilder.anUiExperiment;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExperimentsControllerTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final GuineapigExperimentMgmtService experimentService = context.mock(GuineapigExperimentMgmtService.class);
    private final SpecService specService = context.mock(SpecService.class);
    private final HardCodedScopesProvider hardCodedScopesProvider = new MockHardCodedScopesProvider();
    private final ExperimentsController controller = new ExperimentsController(specService, experimentService, hardCodedScopesProvider, null);

    private final DateTime now = new DateTime();
    private String key = "someKey";
    private UiExperiment uiExperiment = anUiExperiment().
            withType(ExperimentType.AB_TESTING.getType()).
            withGroups(TestGroupMakers.DEFAULT_UI_TEST_GROUPS).
            withKey(key).
            withStartDate(now.getMillis()).
            withEndDate(now.plusMinutes(5).getMillis()).
            withCreationDate(now.getMillis()).
            withScope(MockHardCodedScopesProvider$.MODULE$.HARD_CODED_SPEC_FOR_NON_REG()).
    build();

    @Test
    public void convertsNewExperimentCorrectly() throws Exception {
        final SpecDefinition.ExperimentSpecBuilder nonPersistentSpec = SpecDefinition.ExperimentSpecBuilder.
                aNewlyGeneratedExperimentSpec(key).withPersistent(false);

        givenSpecsForUiExperiment(nonPersistentSpec.build(), uiExperiment);

        Experiment converted = controller.convertToExperiment(uiExperiment, "someUser", true);
        assertThat(converted, is(allOf(hasPersistence(false), hasKey(key), hasCreationDateAfter(now))));
    }

    @Test
    public void convertsNonNewExperimentCorrectly() throws Exception {
        final SpecDefinition.ExperimentSpecBuilder persistentSpec = SpecDefinition.ExperimentSpecBuilder.
                aNewlyGeneratedExperimentSpec(key).withPersistent(true);

        givenSpecsForUiExperiment(persistentSpec.build(), uiExperiment);

        Experiment converted = controller.convertToExperiment(uiExperiment, "someUser", false);
        assertThat(converted, is(allOf(hasPersistence(true), hasKey(key), hasCreationDate(now))));
    }

    private void givenSpecsForUiExperiment(ExperimentSpec specs, UiExperiment uiExperiment) {
        context.checking(new Expectations() {{
            allowing(experimentService).getSpecForExperiment(uiExperiment.getKey());
            will(returnValue(specs));
        }});
    }

    private Matcher<? super Experiment> hasKey(final String key) {
        return new TypeSafeMatcher<Experiment>() {
            @Override
            protected boolean matchesSafely(Experiment item) {
                return key.equals(item.getKey());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An experiment with key [" + key + "]");
            }
        };
    }

    private Matcher<? super Experiment> hasPersistence(final boolean isPersistent) {
        return new TypeSafeMatcher<Experiment>() {
            @Override
            protected boolean matchesSafely(Experiment item) {
                return isPersistent == item.isPersistent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An experiment with persistence [" + isPersistent + "]");
            }
        };
    }

    private Matcher<? super Experiment> hasCreationDate(final DateTime dateTime) {
        return new TypeSafeMatcher<Experiment>() {
            @Override
            protected boolean matchesSafely(Experiment item) {
                return dateTime.isEqual(item.getCreationDate());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An experiment with creation date [" + dateTime + "]");
            }
        };
    }

    private Matcher<? super Experiment> hasCreationDateAfter(final DateTime dateTime) {
        return new TypeSafeMatcher<Experiment>() {
            @Override
            protected boolean matchesSafely(Experiment item) {
                return item.getCreationDate().isAfter(dateTime) || item.getCreationDate().isEqual(dateTime);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An experiment with creation date AFTER [" + dateTime + "]");
            }
        };
    }
}
