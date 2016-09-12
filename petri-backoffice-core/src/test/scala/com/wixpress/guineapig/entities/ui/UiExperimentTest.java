package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.guineapig.dsl.TestGroupMakers;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 1/5/14
 */
public class UiExperimentTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    UiExperiment expected = UiExperimentBuilder.anUiExperiment()
            .withCreationDate(new DateTime().getMillis())
            .withCreator("creator")
            .withDescription("bla bla bla")
            .withEditable(false)
            .withEndDate(new DateTime().plusYears(1).getMillis())
            .withStartDate(new DateTime().plusYears(1).getMillis())
            .withGeo(asList("IL"))
            .withid(1)
            .withOriginalId(1)
            .withLinkId(7)
            .withGroups(TestGroupMakers.DEFAULT_UI_TEST_GROUPS)
            .withName("experiment")
            .withScope("editor")
            .withType(ExperimentType.FEATURE_TOGGLE.getType())
            .withKey("specName")
            .withState(ExperimentState.ACTIVE.getState())
            .withAnonymous(true)
            .withSpecKey(false)
            .withComment("comment")
            .withUpdater("someone@wix.com")
            .withIncludeUserAgentRegexes(asList("(.*)Android(.*)"))
            .withExcludeUserAgentRegexes(asList("(.*)Chrome(.*)"))
            .withExcludeMetaSiteIds(true)
            .withMetaSiteIds(asList(UUID.randomUUID().toString()))
            .withExcludeUserGroups(asList("someGroup"))
            .build();
    @Test
    public void canBeSerialized() throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();

        String json = objectMapper.writeValueAsString(expected);
        UiExperiment actual = objectMapper.readValue(json, new TypeReference<UiExperiment>() {
        });
        assertThat(actual, is(expected));
    }

    @Test
    public void canBeDeserializedWithUnknownField() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();

        String jsonWithUnknownField = "{\"Unknown\":\"\",\"name\":\"experiment\",\"type\":\"featureToggle\",\"creator\":\"creator\",\"scope\":\"editor\",\"state\":\"active\",\"id\":1,\"lastUpdated\":0,\"key\":\"specName\",\"creationDate\":1391430227481,\"description\":\"bla bla bla\",\"startDate\":1422966227506,\"endDate\":1422966227505,\"paused\":false,\"groups\":[{\"id\":0,\"chunk\":0,\"value\":null}],\"editable\":false,\"users\":\"Anonymous Only\",\"geo\":[\"IL\"],\"languages\":[],\"timeZoneOffset\":0,\"usersFilterOptions\":[],\"parentStartTime\":-1,\"openToAll\":true,\"wixUsers\":false}";
        UiExperiment deSerialized = objectMapper.readValue(jsonWithUnknownField, new TypeReference<UiExperiment>() {
        });
        assertThat(deSerialized, is(notNullValue()));
    }

    @Test
    public void canBeDeserializedWithMissingField() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();

        String jsonWithoutNameField = "{\"type\":\"featureToggle\",\"creator\":\"creator\",\"scope\":\"editor\",\"state\":\"active\",\"id\":1,\"lastUpdated\":0,\"key\":\"specName\",\"creationDate\":1391430227481,\"description\":\"bla bla bla\",\"startDate\":1422966227506,\"endDate\":1422966227505,\"paused\":false,\"groups\":[{\"id\":0,\"chunk\":0,\"value\":null}],\"editable\":false,\"users\":\"Anonymous Only\",\"geo\":[\"IL\"],\"languages\":[],\"timeZoneOffset\":0,\"usersFilterOptions\":[],\"parentStartTime\":-1,\"openToAll\":true,\"wixUsers\":false}";
        UiExperiment deSerialized = objectMapper.readValue(jsonWithoutNameField, new TypeReference<UiExperiment>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.getName(), is(""));
    }

    @Test
    public void canBeCopied()  {
        UiExperiment copied = UiExperimentBuilder.aCopyOf(expected).build();
        assertThat(copied, is(expected));
    }
}
