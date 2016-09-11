package com.wixpress.guineapig.services;

import com.google.common.collect.ImmutableList;
import com.wixpress.guineapig.entities.ExperimentBuilders;
import com.wixpress.guineapig.entities.ui.UiSpec;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.petri.FullPetriClient;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionForAllUserTypes;
import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionOnlyForLoggedInUsers;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SpecServiceTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final FullPetriClient fullPetriClient = context.mock(FullPetriClient.class);
    private final PetriSpecService specsService = new PetriSpecService(fullPetriClient);

    private final String specKey =  "f.q.n";
    private final ExperimentSpec SOME_SPEC = aNewlyGeneratedExperimentSpec(specKey).
            withTestGroups(asList("on", "off")).
            withOwner("talyag@wix.com").
            withScopes(
                    aScopeDefinitionOnlyForLoggedInUsers("scope1"),
                    aScopeDefinitionForAllUserTypes("scope2")).
            withPersistent(false).
            build();

    @Test
    public void getAllSpecsReturnsUiSpecs() throws Exception {

        context.checking(new Expectations() {{
            allowing(fullPetriClient).fetchSpecs();
            will(returnValue(ImmutableList.of(SOME_SPEC)));
        }
            {
                allowing(fullPetriClient).fetchAllExperiments();
                will(returnValue(ImmutableList.of()));
            }
        });

        UiSpec expected = new UiSpec("f.q.n", asList("on", "off"), "talyag@wix.com",
                SOME_SPEC.getCreationDate().getMillis(), SOME_SPEC.getUpdateDate().getMillis(),
                asList("scope1", "scope2"), true, false);

        List<UiSpec> uiSpecs = specsService.getAllSpecs();
        assertThat(uiSpecs.size(), is(1));
        assertThat(uiSpecs.get(0), is(expected));
    }

    @Test
    public void getAllSpecsWhenSpecIsUsedReturnsUISpecWithFalseCanBeDeleted() throws Exception {

        final Experiment experiment = ExperimentBuilders.createActiveEditorExperiment(specKey).build();

        context.checking(new Expectations() {
            {
                allowing(fullPetriClient).fetchSpecs();
                will(returnValue(ImmutableList.of(SOME_SPEC)));
            }

            {
                allowing(fullPetriClient).fetchAllExperiments();
                will(returnValue(ImmutableList.of(experiment)));
            }
        });

        List<UiSpec> uiSpecs = specsService.getAllSpecs();
        assertThat(uiSpecs.get(0).isCanBeDeleted(), is(false));
    }

    @Test
    public void deleteSpec() throws Exception {

        final String specKey = "spec1";

        context.checking(new Expectations() {{
            oneOf(fullPetriClient).deleteSpec(specKey);
        }});

        specsService.deleteSpec(specKey);

    }

    @Test
    public void addSpecs() throws Exception {

        final List<ExperimentSpec> specLists = ImmutableList.of(SOME_SPEC);

        context.checking(new Expectations() {{
            oneOf(fullPetriClient).addSpecs(specLists);
        }});

        specsService.addSpecs(specLists);

    }


    @Test
    public void isSpecActiveShouldReturnTrueWhenActiveExperimentExists(){
       assertThat(specsService.isSpecActive(specKey, ImmutableList.of(ExperimentBuilders.createActiveEditorExperiment(specKey).build())), is(true));

    }

    @Test
    public void isSpecActiveShouldReturnFalseWhenExperimentListIsEmpty(){
       assertThat(specsService.isSpecActive(specKey, ImmutableList.<Experiment>of()), is(false));

    }

    @Test
    public void isSpecActiveShouldReturnFalseWhenNoExperimentWithSpecKeyExists(){
       assertThat(specsService.isSpecActive(specKey, ImmutableList.of(ExperimentBuilders.createActiveEditorExperiment("newSpec").build())), is(false));

    }

    @Test
    public void isSpecActiveShouldReturnFalseWhenExperimentIsAlreadyTerminated(){
       assertThat(specsService.isSpecActive(specKey, ImmutableList.of(ExperimentBuilders.createTerminatedEditorExperiment(specKey).build())), is(false));

    }

    @Test
    public void isSpecActiveShouldReturnFalseWhenExperimentIsNotFromSpec(){
       assertThat(specsService.isSpecActive(specKey, ImmutableList.of(ExperimentBuilders.createActiveEditorExperimentNotFromSpec(specKey).build())), is(false));
    }

}