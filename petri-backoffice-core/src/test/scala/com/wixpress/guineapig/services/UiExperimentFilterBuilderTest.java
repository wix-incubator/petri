package com.wixpress.guineapig.services;

import com.wixpress.guineapig.entities.ui.*;
import com.wixpress.guineapig.spi.FilterAdapterExtender;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.hamcrest.CoreMatchers;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UiExperimentFilterBuilderTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private FilterAdapterExtender filterAdapterExtender = context.mock(FilterAdapterExtender.class);
    private final ExperimentConverter experimentConverter =
            new ExperimentConverter(new AlwaysTrueIsEditablePredicate(), filterAdapterExtender);

    @Before
    public void noExtension(){
        context.checking(new Expectations() {{
            oneOf(filterAdapterExtender).extendUiExperiment(with(any(List.class)), with(CoreMatchers.any(UiExperimentBuilder.class)));
        }});
    }

    //from server to ui  ...
    @Test
    public void verifyNoFilterPassedToUi() throws IOException, ClassNotFoundException {
        Experiment experiment = make(an(ExperimentMakers.Experiment));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(false));
        assertThat(uiExperiment.isNewRegistered(), is(false));
        assertThat(uiExperiment.isAnonymous(), is(false));
        assertThat(uiExperiment.isWixUsers(), is(false));
        assertThat(uiExperiment.getGeo().size(), is(0));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));

    }

    @Test
    public void WixUsersFiltrPassedToUi() throws IOException, ClassNotFoundException {

        Filter f = new WixEmployeesFilter();
        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(false));
        assertThat(uiExperiment.isNewRegistered(), is(false));
        assertThat(uiExperiment.isAnonymous(), is(false));
        assertThat(uiExperiment.isWixUsers(), is(true));
        assertThat(uiExperiment.getGeo().size(), is(0));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));

    }

    @Test
    public void WixAnonymouseFiltrPassedToUi() throws IOException, ClassNotFoundException {

        Filter f = new FirstTimeVisitorsOnlyFilter();
        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(false));
        assertThat(uiExperiment.isNewRegistered(), is(false));
        assertThat(uiExperiment.isAnonymous(), is(true));
        assertThat(uiExperiment.isWixUsers(), is(false));
        assertThat(uiExperiment.getGeo().size(), is(0));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));
    }

    @Test
    public void WixRegisteredFiltrPassedToUi() throws IOException, ClassNotFoundException {

        Filter f = new RegisteredUsersFilter();
        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(true));
        assertThat(uiExperiment.isNewRegistered(), is(false));
        assertThat(uiExperiment.isAnonymous(), is(false));
        assertThat(uiExperiment.isWixUsers(), is(false));
        assertThat(uiExperiment.getGeo().size(), is(0));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));
    }


    @Test
    public void NewResisteredFiltrPassedToUi() throws IOException, ClassNotFoundException {

        Filter f = new NewUsersFilter();
        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(false));
        assertThat(uiExperiment.isNewRegistered(), is(true));
        assertThat(uiExperiment.isAnonymous(), is(false));
        assertThat(uiExperiment.isWixUsers(), is(false));
        assertThat(uiExperiment.getGeo().size(), is(0));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));
    }

    @Test
    public void WixGeoFiltrPassedToUi() throws IOException, ClassNotFoundException {

        Filter f = new GeoFilter(Arrays.asList(Locale.getISOCountries()));
        List<String> fullList = ((GeoFilter) f).getCountries();

        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertThat(uiExperiment.isAllRegistered(), is(false));
        assertThat(uiExperiment.isNewRegistered(), is(false));
        assertThat(uiExperiment.isAnonymous(), is(false));
        assertThat(uiExperiment.isWixUsers(), is(false));
        assertThat(uiExperiment.getLanguages().size(), is(0));
        assertThat(uiExperiment.getIncludeGuids().size(), is(0));
        assertThat(uiExperiment.getExcludeGuids().size(), is(0));
        for (int i = 0; i < fullList.size(); i++) {
            assertEquals(fullList.get(i), uiExperiment.getGeo().get(i));
        }
    }

    @Test
    public void includeUidsFilterPassedToUi() throws IOException, ClassNotFoundException {
        UUID userGuid = UUID.randomUUID();
        Filter f = new IncludeUserIdsFilter(userGuid);

        Experiment experiment = make(an(ExperimentMakers.Experiment
                , with(ExperimentMakers.filters, Arrays.asList(f))));

        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        assertEquals(uiExperiment.getIncludeGuids(), asList(userGuid.toString()));
    }
}