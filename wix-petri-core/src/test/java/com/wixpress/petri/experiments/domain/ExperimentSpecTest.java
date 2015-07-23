package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.SpecDefinition;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionForAllUserTypes;
import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionOnlyForLoggedInUsers;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ExperimentSpecTest {

    @Test
    public void canBeSerializedFromOld() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithNoScopesField = "{\"fqn\":\"f.q.n.Class1\",\"testGroups\":[\"1\",\"2\"],\"creationDate\":\"2013-12-05T11:55:55.828+02:00\"}";
        ExperimentSpec deSerialized = objectMapper.readValue(jsonWithNoScopesField, new TypeReference<ExperimentSpec>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.getScopes().size(), is(0));
    }

    @Test
    public void canBeSerializedFromNew() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownField = "{\"unknown\":\"whatever\",\"creationDate\":\"2014-06-25T16:55:08.508+03:00\",\"updateDate\":\"2014-06-25T16:55:08.508+03:00\",\"owner\":\"talya\",\"testGroups\":[\"1\",\"2\"],\"persistent\":true,\"key\":\"f.q.n.Class1\",\"scopes\":[{\"name\":\"scope1\",\"onlyForLoggedInUsers\":true},{\"name\":\"scope2\",\"onlyForLoggedInUsers\":false}]}";
        ExperimentSpec deSerialized = objectMapper.readValue(jsonWithUnknownField, new TypeReference<ExperimentSpec>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.getUpdateDate(), is(notNullValue()));
    }

    @Test
    public void canBeSerializedWithCreationTime() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        SpecDefinition.ExperimentSpecBuilder experimentSpecBuilder = aNewlyGeneratedExperimentSpec("f.q.n.Class1").
                withTestGroups(asList("1", "2")).
                withOwner("talya").
                withPersistent(false);
        ExperimentSpec experimentSpec = experimentSpecBuilder.build().setCreationDate(new DateTime().plusDays(3));
        String json = objectMapper.writeValueAsString(experimentSpec);
        ExperimentSpec deSerialized = objectMapper.readValue(json, new TypeReference<ExperimentSpec>() {
        });

        assertThat(deSerialized, is(experimentSpec));
    }

    @Test
    public void canBeSerializedWithScopeDefinitions() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        ExperimentSpec experimentSpec = aNewlyGeneratedExperimentSpec("f.q.n.Class1").
                withTestGroups(asList("1", "2")).
                withScopes(aScopeDefinitionOnlyForLoggedInUsers("scope1"), aScopeDefinitionForAllUserTypes("scope2")).
                withOwner("talya").
                build();
        String json = objectMapper.writeValueAsString(experimentSpec);
        ExperimentSpec deSerialized = objectMapper.readValue(json, new TypeReference<ExperimentSpec>() {
        });

        assertThat(deSerialized, is(experimentSpec));
        assertThat(deSerialized.getScopes(), is(asList(new ScopeDefinition("scope1", true),
                new ScopeDefinition("scope2", false))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotBeCreatedWithNullScope() throws IOException {
        aNewlyGeneratedExperimentSpec("f.q.n.Class1").
                withScopes(aScopeDefinitionOnlyForLoggedInUsers("scope1"), null).
                build();
    }

}
