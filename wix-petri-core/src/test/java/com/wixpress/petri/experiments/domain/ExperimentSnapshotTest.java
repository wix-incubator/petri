package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.TestGroup;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author sagyr
 * @since 9/16/13
 */
public class ExperimentSnapshotTest {

    private ExperimentSnapshotBuilder snapshotWithGroups(List<TestGroup> twoGroupToggle) {
        return anExperimentSnapshot().withOnlyForLoggedInUsers(true).withGroups(twoGroupToggle);
    }

    @Test
    public void canBeSerialized() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        ExperimentSnapshot snapshot = anExperimentSnapshot().
                withKey("key").
                withDescription("lbl").
                withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).
                withScopes(ImmutableList.of("editor")).
                withOnlyForLoggedInUsers(true).build();

        String json = objectMapper.writeValueAsString(snapshot);
        ExperimentSnapshot deSerialized = objectMapper.readValue(json, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(snapshot));
    }

    @Test
    public void canBeSerializedFromOldVersionWithMissingFields() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithNoFiltersField = "{\"key\":\"key\",\"creationDate\":\"2013-12-04T14:56:34.039+02:00\",\"description\":\"lbl\",\"startDate\":\"2013-12-04T14:56:34.063+02:00\",\"endDate\":\"2013-12-04T14:56:34.063+02:00\",\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\"},{\"id\":2,\"chunk\":50,\"value\":\"\"}],\"scope\":\"somescope\",\"onlyForLoggedInUsers\":\"true\"}";
        ExperimentSnapshot deSerialized = objectMapper.readValue(jsonWithNoFiltersField, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.filters().size(), is(0));
        assertThat(deSerialized.scopes(), contains("somescope"));
    }

    @Test
    public void canBeSerializedFromVersionWithSingleScope() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWitSingleScopeField = "{\"key\":\"key\",\"creationDate\":\"2013-12-04T14:56:34.039+02:00\",\"description\":\"lbl\",\"startDate\":\"2013-12-04T14:56:34.063+02:00\",\"endDate\":\"2013-12-04T14:56:34.063+02:00\",\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\"},{\"id\":2,\"chunk\":50,\"value\":\"\"}],\"filters\":[],\"scope\":\"somescope\",\"onlyForLoggedInUsers\":\"true\"}";
        ExperimentSnapshot deSerialized = objectMapper.readValue(jsonWitSingleScopeField, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.filters().size(), is(0));
        assertThat(deSerialized.scopes(), contains("somescope"));
    }


    @Test
    public void canBeSerializedFromNewVersionWithUnkownFiled() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownField = "{\"unknown\":\"key\",\"key\":\"key\",\"creationDate\":\"2013-12-04T14:56:34.039+02:00\",\"description\":\"lbl\",\"startDate\":\"2013-12-04T14:56:34.063+02:00\",\"endDate\":\"2013-12-04T14:56:34.063+02:00\",\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\"},{\"id\":2,\"chunk\":50,\"value\":\"\"}],\"scopes\":[\"somescope\"],\"filters\":[],\"onlyForLoggedInUsers\":\"true\", \"someUnkonwn\":[]}";
        ExperimentSnapshot deSerialized = objectMapper.readValue(jsonWithUnknownField, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.scopes(), contains("somescope"));
        assertThat(deSerialized.scope(), is("somescope"));
    }

    @Test
    public void canBeSerializedWithFilters() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        ExperimentSnapshot snapshot = anExperimentSnapshot().withOnlyForLoggedInUsers(true).
                withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).
                withFilters(asList(new NotFilter(new GeoFilter(asList("us", "gb"))), new FirstTimeVisitorsOnlyFilter())).build();
        String json = objectMapper.writeValueAsString(snapshot);
        ExperimentSnapshot deSerialized = objectMapper.readValue(json, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(snapshot));
    }

    @Test
    public void deserializedWithUnknownFilterIsNotValid() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownFilter = "{\"key\":\"\",\"creationDate\":\"2014-02-03T15:13:37.505+02:00\",\"description\":\"\",\"name\":\"\",\"startDate\":\"2014-02-03T15:13:37.504+02:00\",\"endDate\":\"2015-02-03T15:13:37.504+02:00\",\"paused\":false,\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null},{\"id\":2,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null}],\"scope\":\"\",\"filters\":[{\"filter-type\":\"anonymous\"},{\"filter-type\":\"geoo\"}],\"creator\":\"\",\"seed\":0,\"featureToggle\":false,\"onlyForLoggedInUsers\":\"true\"}";
        ExperimentSnapshot deSerialized = objectMapper.readValue(jsonWithUnknownFilter, new TypeReference<ExperimentSnapshot>() {
        });

        assertFalse("snapshot should be valid, unknown filter", deSerialized.isValid());
    }

    @Test
    public void deserializedWithUnknownFilterInsideNotIsNotValid() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownFilter = "{\"key\":\"\",\"creationDate\":\"2014-02-03T15:13:37.505+02:00\",\"description\":\"\",\"name\":\"\",\"startDate\":\"2014-02-03T15:13:37.504+02:00\",\"endDate\":\"2015-02-03T15:13:37.504+02:00\",\"paused\":false,\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null},{\"id\":2,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null}],\"scope\":\"\",\"filters\":[{\"filter-type\":\"anonymous\"},{\"filter-type\":\"not\",\"internal\":{\"filter-type\":\"geoo\"}}],\"creator\":\"\",\"seed\":0,\"featureToggle\":false,\"onlyForLoggedInUsers\":\"true\"}";
        ExperimentSnapshot deSerialized = objectMapper.readValue(jsonWithUnknownFilter, new TypeReference<ExperimentSnapshot>() {
        });

        assertFalse("snapshot should be valid, unknown filter", deSerialized.isValid());
    }

    @Test
    public void assignsUniqueIdsToTestGroupsIfMissing() {
        List<com.wixpress.petri.experiments.domain.TestGroup> groupsWithNoID = asList(
                a(TestGroup,
                        with(groupId, 0),
                        with(probability, 50)).
                        make(),
                a(TestGroup,
                        with(groupId, 0),
                        with(probability, 50)).
                        make()
        );
        assertThat(snapshotWithGroups(groupsWithNoID).build(),
                is(snapshotWithGroups(VALID_TEST_GROUP_WITH_NO_VALUES_LIST).build()));
    }

    @Test
    public void assignsUniqueIdsToTestGroupsIfSomeAreMissing() {
        List<com.wixpress.petri.experiments.domain.TestGroup> groupsWithPartialIDs = asList(
                a(TestGroup,
                        with(groupId, 1),
                        with(probability, 50)).
                        make(),
                a(TestGroup,
                        with(groupId, 0),
                        with(probability, 50)).
                        make()
        );
        assertThat(snapshotWithGroups(groupsWithPartialIDs).build(),
                is(snapshotWithGroups(VALID_TEST_GROUP_WITH_NO_VALUES_LIST).build()));
    }

    @Test
    public void canBeSerializedWithForRegisteredOnly() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        ExperimentSnapshot snapshot = anExperimentSnapshot().
                withOnlyForLoggedInUsers(true).
                withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).
                build();
        String json = objectMapper.writeValueAsString(snapshot);
        ExperimentSnapshot deSerialized = objectMapper.readValue(json, new TypeReference<ExperimentSnapshot>() {
        });
        assertThat(deSerialized, is(snapshot));
    }


}
