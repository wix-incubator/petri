package com.wixpress.petri.petri;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.experiments.domain.ScopeDefinition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/2/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SpecDefinition {

    public ExperimentSpec create(DateTime updateDate) {
        ExperimentSpecBuilder builder = new ExperimentSpecBuilder(this.getClass().getName(), updateDate);
        customize(builder);
        return builder.build();
    }

    protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
        return builder;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExperimentSpecBuilder {
        private final String key;
        private final DateTime creationDate;
        private final DateTime updateDate;
        private List<String> testGroups = new ArrayList<String>();
        private List<ScopeDefinition> scopes = new ArrayList<ScopeDefinition>();
        private String owner = "";
        private boolean persistent = true;
        private boolean allowedForBots = false;

        public ExperimentSpecBuilder(String key, DateTime updateDate) {
            this(key, updateDate, updateDate);
        }

        @JsonCreator
        public ExperimentSpecBuilder(@JsonProperty(value = "key") String key,
                                     @JsonProperty(value = "creationDate") DateTime creationDate,
                                     @JsonProperty(value = "updateDate") DateTime updateTime) {
            this.key = key;
            this.creationDate = creationDate.withZone(DateTimeZone.UTC);
            if (updateTime == null) {
                this.updateDate = creationDate;
            } else {
                this.updateDate = updateTime.withZone(DateTimeZone.UTC);
            }
        }

        public static ExperimentSpecBuilder anExperimentSpec(String key, DateTime updateDate) {
            return new ExperimentSpecBuilder(key, updateDate);
        }

        public static ExperimentSpecBuilder aNewlyGeneratedExperimentSpec(String key) {
            return anExperimentSpec(key, new DateTime());
        }

        public ExperimentSpecBuilder withTestGroups(List<String> testGroups) {
            this.testGroups = testGroups;
            return this;
        }

        /**
         * A list of all relevant scopes. Usually only one.
         * EVERY SCOPE NAME SHOULD ONLY BE DECLARED ONCE!
         * for example :
         * withScopes(
         * ScopeDefinition.aScopeDefinitionOnlyForLoggedInUsers("dashboard"),
         * ScopeDefinition.aScopeDefinitionForAllUserTypes("public")
         * );
         * Use aScopeDefinitionOnlyForLoggedInUsers - If the experiment is always conducted for logged in users - i.e in the dashboard, editor etc.
         * Use aScopeDefinitionForAllUserTypes      - If the experiment can be conducted for non-logged in users - i.e in the renderer, template-viewer etc.
         */
        public ExperimentSpecBuilder withScopes(ScopeDefinition... scopes) {
            this.scopes = newArrayList(scopes);
            return this;
        }

        /**
         * @param ownerEmail valid email address
         */
        public ExperimentSpecBuilder withOwner(String ownerEmail) {
            this.owner = ownerEmail;
            return this;
        }

        /**
         * @param persistent whether conduction value should be persisted (by cookies or other method)
         *                   default is true
         */
        public ExperimentSpecBuilder withPersistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        /**
         * @param allowedForBots whether conduction should be made for bots requests .
         *                       DO NOT USE unless you have a specific seo use case
         *                   default is false
         */
        public ExperimentSpecBuilder withAllowedForBots(boolean allowedForBots) {
            this.allowedForBots = allowedForBots;
            return this;
        }


        public ExperimentSpec build() {
            if (scopes.contains(null)) {
                throw new IllegalArgumentException("cannot create spec with null scope");
            }
            return new ExperimentSpec(key, owner, testGroups, creationDate, scopes, updateDate, persistent, allowedForBots);
        }

    }
}

