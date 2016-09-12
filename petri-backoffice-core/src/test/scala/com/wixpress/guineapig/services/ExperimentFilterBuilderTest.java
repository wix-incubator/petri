package com.wixpress.guineapig.services;

import com.natpryce.makeiteasy.Property;
import com.wixpress.guineapig.dsl.UiExperimentMakers;
import com.wixpress.guineapig.entities.ui.ExperimentFilterBuilder;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.petri.experiments.domain.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ExperimentFilterBuilderTest {

    //from ui to server

    private void verifyGeneratedUsersFilter(Property<UiExperiment, Boolean> filterToAdd, Filter expectedFilter) throws IOException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(filterToAdd, true)));

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(asList(expectedFilter)));
    }

    @Test
    public void userFilterCreated() throws IOException, ClassNotFoundException {
        verifyGeneratedUsersFilter(UiExperimentMakers.allRegistered, new RegisteredUsersFilter());
    }

    @Test
    public void excludeMetaSiteIdFilterCreated() throws IOException, ClassNotFoundException {
        verifyGeneratedUsersFilter(UiExperimentMakers.allRegistered, new RegisteredUsersFilter());
    }

    @Test
    public void anonymousFilterCreated() throws IOException, ClassNotFoundException {
        verifyGeneratedUsersFilter(UiExperimentMakers.anonymous, new FirstTimeVisitorsOnlyFilter());
    }

    @Test
    public void newUserFilterCreated() throws IOException, ClassNotFoundException {
        verifyGeneratedUsersFilter(UiExperimentMakers.newRegistered, new NewUsersFilter());
    }

    @Test
    public void verifyWixUserFilterExpected() throws IOException, ClassNotFoundException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.wixUsers, true)));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new WixEmployeesFilter());

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }

    @Test
    public void verifyIncludeGeoFilterExpected() throws IOException, ClassNotFoundException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.geo, Arrays.asList("IL"))));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new GeoFilter(Arrays.asList("IL")));

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }

    @Test
    public void verifyExcludeGeoFilterExpected() throws IOException, ClassNotFoundException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.geo, Arrays.asList("IL"))
                , with(UiExperimentMakers.excludeGeo, true)
                , with(UiExperimentMakers.hosts, Arrays.asList("host1", "host2"))));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new NotFilter(new GeoFilter(Arrays.asList("IL"))));
        filters.add(new HostFilter(Arrays.asList("host1", "host2")));

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }


    @Test
    public void verifyUserAgentRegexFilterExpected() throws IOException, ClassNotFoundException {
        String androidRegex = "(.*)Android(.*)";
        String chromeRegex = "(.*)Chrome(.*)";
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.includeUserAgentRegexes, Arrays.asList(androidRegex))
                , with(UiExperimentMakers.excludeUserAgentRegexes, Arrays.asList(chromeRegex))
        ));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new UserAgentRegexFilter(Arrays.asList(androidRegex), Arrays.asList(chromeRegex)));
        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }

    @Test
    public void verifyNonRegisteredUsersFilterExpected() throws IOException, ClassNotFoundException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.nonRegistered, true))
        );

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new NonRegisteredUsersFilter());
        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }


    @Test
    public void includeUidsFilterExpected() throws IOException, ClassNotFoundException {
        UUID uid = UUID.randomUUID();
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.includeGuids, asList(uid.toString()))));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new IncludeUserIdsFilter(uid));

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment), is(filters));
    }

    @Test
    public void verifyUserNotInAnyGroupFilterExpected() throws IOException, ClassNotFoundException {
        List<String> excludedGroups = asList("group1");
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment
                , with(UiExperimentMakers.excludeUserGroups, excludedGroups)));

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new UserNotInAnyGroupFilter(excludedGroups));

        assertThat(ExperimentFilterBuilder.extractFiltersFromUiExperiment(uiExperiment),is(filters));
    }
}