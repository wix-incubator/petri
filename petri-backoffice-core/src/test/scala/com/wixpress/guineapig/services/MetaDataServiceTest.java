package com.wixpress.guineapig.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.wixpress.guineapig.dao.MetaDataDao;
import com.wixpress.guineapig.dto.SpecExposureIdViewDto;
import com.wixpress.guineapig.dto.UserAgentRegex;
import com.wixpress.guineapig.entities.ui.FilterOption;
import com.wixpress.guineapig.entities.ui.UiSpecForScope;
import com.wixpress.guineapig.spi.*;
import com.wixpress.guineapig.util.MockHardCodedScopesProvider;
import com.wixpress.guineapig.util.MockHardCodedScopesProvider$;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.experiments.domain.ScopeDefinition;
import com.wixpress.petri.petri.FullPetriClient;
import org.jmock.AbstractExpectations;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import scala.Some;
import scala.collection.JavaConversions;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MetaDataServiceTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    MetaDataDao metaDataDao = context.mock(MetaDataDao.class);
    SpecExposureIdRetriever specExposureIdDao = context.mock(SpecExposureIdRetriever.class);
    FullPetriClient fullPetriClient = context.mock(FullPetriClient.class);
    Set<String> languagesSet = ImmutableSet.of("en", "fr", "de", "es", "pt");
    SupportedLanguagesProvider supportedSupportedLanguagesProvider = new MockSupportedSupportedLanguagesProvider(languagesSet);
    GlobalGroupsManagementService globalGroupsManagementService = context.mock(GlobalGroupsManagementService.class);
    HardCodedScopesProvider hardCodedScopesProvider = new MockHardCodedScopesProvider();

    MetaDataService metaDataService = new MetaDataService(
            metaDataDao, fullPetriClient, hardCodedScopesProvider, specExposureIdDao, supportedSupportedLanguagesProvider, globalGroupsManagementService
    );
    private final String EDITOR_SCOPE = "editor";
    ScopeDefinition editorScope = new ScopeDefinition(EDITOR_SCOPE, true);
    private final String VIEWER_SCOPE = "viewer";
    ScopeDefinition viewerScope = new ScopeDefinition(VIEWER_SCOPE, false);
    private final ExperimentSpec viewerEditorSpec = aNewlyGeneratedExperimentSpec("f.q.n.Class1").
            withTestGroups(asList("1", "2")).
            withScopes(editorScope, viewerScope).build();

    @Test
    public void geoListContainsAllCountries() {
        //ui supply code list examplw : ["IL","FR"]
        //ui expects list of geo objects [{id:"IL",text:"Israel"},{id:"FR",text:"France"}]
        List<FilterOption> fullList = metaDataService.getGeoList();
        assertEquals(fullList.size(), Locale.getISOCountries().length + MetaDataService.COUNTRIES_GROUPS().size());
        assertThat(fullList.get(0), is(MetaDataService.COUNTRIES_GROUPS().get(0)));
    }

    @Test
    public void userAgentRegexListReadsValuesFromDao() {
        final UserAgentRegex userAgentRegex = new UserAgentRegex("*test", "This is a test");

        context.checking(new Expectations() {{
            allowing(metaDataDao).get(UserAgentRegex.class);
            will(AbstractExpectations.returnValue(ImmutableList.of(userAgentRegex)));
        }});

        List<FilterOption> fullList = metaDataService.getUserAgentRegexList();
        assertEquals(fullList.get(0).id(), userAgentRegex.regex());
        assertEquals(fullList.get(0).text(), userAgentRegex.description());
        assertThat(fullList.size(), is(1));
    }


    @Test
    public void userGroupsListReadFromGlobalGroupsManagementService() {
        ImmutableList<String> userGroupsList = ImmutableList.of("group1");

        context.checking(new Expectations() {{
            allowing(globalGroupsManagementService).allGlobalGroups();
            will(AbstractExpectations.returnValue(JavaConversions.asScalaBuffer(userGroupsList)));
        }});

        assertEquals(metaDataService.getUserGroupsList().get(0).id(), userGroupsList.get(0));
    }

    @Test
    public void userGroupsListReturnsEmptyListWhenReadFromGlobalGroupsManagementServiceFails() {
        context.checking(new Expectations() {{
            allowing(globalGroupsManagementService).allGlobalGroups();
            will(AbstractExpectations.throwException(new NullPointerException()));
        }});

        assertThat(metaDataService.getUserGroupsList().size(), is(0));
    }

    @Test
    public void languagesListContainsAllWixLanguages() {

        List<FilterOption> fullList = metaDataService.getLangList();
        assertEquals(fullList.size(), languagesSet.size());
    }

    @Test
    public void specWithMultipleScopesIsAddedCorrectly() {
        context.checking(new Expectations() {{

            allowing(specExposureIdDao).getAll();
            will(AbstractExpectations.returnValue(Collections.EMPTY_LIST));

        }});
        ScopeDefinition newRegisteredScope = new ScopeDefinition("new-scope", true);
        ExperimentSpec multipleScopeSpec = aNewlyGeneratedExperimentSpec("f.q.n.Class1").
                withTestGroups(asList("1", "2")).
                withScopes(editorScope, newRegisteredScope).build();
        scala.collection.immutable.Map<String, scala.collection.immutable.List<UiSpecForScope>> map =
                metaDataService.createScopeToSpecMap(asList(multipleScopeSpec));

        assertThat(map.contains(EDITOR_SCOPE + ",new-scope"), is(true));

    }

    @Test
    public void editorScopeIsAddedWhenKeyCaseChanged() {
        context.checking(new Expectations() {{

            allowing(specExposureIdDao).getAll();
            will(AbstractExpectations.returnValue(Collections.EMPTY_LIST));

        }});
        ExperimentSpec viewerEditorSpec = aNewlyGeneratedExperimentSpec("SPEC_KEY").
                withTestGroups(asList("1", "2")).
                withScopes(editorScope, viewerScope).build();


        scala.collection.immutable.Map<String, scala.collection.immutable.List<UiSpecForScope>> map =
                metaDataService.createScopeToSpecMap(asList(viewerEditorSpec));

        assertThat(map.get(EDITOR_SCOPE).get().head().getKey(), is(viewerEditorSpec.getKey()));

    }

    @Test
    public void scopesMapAddsHardcodedScope() {
        context.checking(new Expectations() {{

            allowing(fullPetriClient).fetchSpecs();
            will(AbstractExpectations.returnValue(ImmutableList.of(viewerEditorSpec)));


            allowing(specExposureIdDao).getAll();
            will(AbstractExpectations.returnValue(Collections.EMPTY_LIST));

            allowing(fullPetriClient).fetchAllExperimentsGroupedByOriginalId();
            will(AbstractExpectations.returnValue(Collections.EMPTY_LIST));
        }});

        java.util.Map<String, List<UiSpecForScope>> map = metaDataService.createScopeToSpecMap();
        assertThat(map.size(), is(4));
        assertTrue(map.containsKey(MockHardCodedScopesProvider$.MODULE$.HARD_CODED_SPEC_FOR_NON_REG()));
    }

    @Test
    public void scopeMapExposureId() {
        final ExperimentSpec specWithExposure = aNewlyGeneratedExperimentSpec("f.q.n.Class1")
                .withScopes(editorScope)
                .build();

        final ExperimentSpec specWithoutExposure = aNewlyGeneratedExperimentSpec("f.q.n.Class2")
                .withScopes(viewerScope)
                .build();
        final SpecExposureIdViewDto specExposure = new SpecExposureIdViewDto(
                "f.q.n.Class1", new Some<>("GUID"), new DateTime(), new DateTime()
        );

        context.checking(new Expectations() {{

            allowing(fullPetriClient).fetchSpecs();
            will(AbstractExpectations.returnValue(asList(specWithExposure, specWithoutExposure)));

            allowing(specExposureIdDao).getAll();
            will(AbstractExpectations.returnValue(asList(specExposure)));

            allowing(fullPetriClient).fetchAllExperimentsGroupedByOriginalId();
            will(AbstractExpectations.returnValue(Collections.EMPTY_LIST));
        }});

        java.util.Map<String, List<UiSpecForScope>> map = metaDataService.createScopeToSpecMap();
        assertThat(map.get(editorScope.getName()).get(0).getExposureId(), is("GUID"));
        assertNull(map.get(viewerScope.getName()).get(0).getExposureId());
    }

    @Test
    public void checkGetSpecExposureMapFromDB() {
        context.checking(new Expectations() {{

            allowing(specExposureIdDao).getAll();
            will(AbstractExpectations.returnValue(asList()));

        }});

        metaDataService.getSpecExposureMapFromDB();
    }


}


