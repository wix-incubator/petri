package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wixpress.petri.experiments.domain.Experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * Date: 12/18/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UiExperimentBuilder {
    private int id = Experiment.NO_ID;
    private String name = "";
    private String type = ExperimentType.FEATURE_TOGGLE.getType();
    private String creator = "";
    private String scope = "";
    private String state = "";
    private long lastUpdated = 0;
    private String key = "";
    private boolean specKey = true;
    private long creationDate = 0;
    private String description = "";
    private String comment = "";
    private String updater = "";
    private long startDate = 0;
    private long endDate = 0;
    private List<UiTestGroup> groups = new ArrayList<UiTestGroup>();
    private boolean editable = true;
    private boolean paused = false;
    private boolean isExcludeGeo = false;
    private List<String> geo = new ArrayList<String>();
    private List<String> includeGuids = new ArrayList<String>();
    private List<String> excludeGuids = new ArrayList<String>();
    private List<String> languages = new ArrayList<String>();
    private List<String> hosts = new ArrayList<String>();
    private boolean wixUsers = false;
    private long parentStartTime = -1;
    private int originalId = Experiment.NO_ID;
    private int linkId = Experiment.NO_ID;
    private boolean allRegistered = false;
    private boolean newRegistered = false;
    private boolean anonymous = false;
    private List<String> includeUserAgentRegexes = new ArrayList<String>();
    private List<String> excludeUserAgentRegexes = new ArrayList<String>();
    private List<String> metaSiteIds = new ArrayList<String>();
    private boolean isExcludeMetaSite = false;
    private int conductLimit;
    private boolean nonRegistered;
    private List<String> excludeUserGroups = new ArrayList<String>();

    public static UiExperimentBuilder anUiExperiment() {
        return new UiExperimentBuilder();
    }

    public UiExperimentBuilder withConductLimit(int limit) {
        this.conductLimit = limit;
        return this;
    }

    public UiExperimentBuilder withid(int id) {
        this.id = id;
        return this;
    }

    public UiExperimentBuilder withOriginalId(int originalId) {
        this.originalId = originalId;
        return this;
    }

    public UiExperimentBuilder withLinkId(int linkId) {
        this.linkId = linkId;
        return this;
    }

    public UiExperimentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UiExperimentBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public UiExperimentBuilder withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public UiExperimentBuilder withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public UiExperimentBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public UiExperimentBuilder withLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public UiExperimentBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public UiExperimentBuilder withSpecKey(boolean specKey) {
        this.specKey = specKey;
        return this;
    }

    public UiExperimentBuilder withCreationDate(long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public UiExperimentBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public UiExperimentBuilder withStartDate(long startDate) {
        this.startDate = startDate;
        return this;
    }

    public UiExperimentBuilder withEndDate(long endDate) {
        this.endDate = endDate;
        return this;
    }

    public UiExperimentBuilder withGroups(List<UiTestGroup> groups) {
        this.groups = groups;
        return this;
    }

    public UiExperimentBuilder withEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public UiExperimentBuilder withPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public UiExperimentBuilder withExcludeGeo(boolean isExcludeGeo) {
        this.isExcludeGeo = isExcludeGeo;
        return this;
    }

    public UiExperimentBuilder withGeo(List<String> geo) {
        this.geo = geo;
        return this;
    }

    public UiExperimentBuilder withLanguages(List<String> languages) {
        this.languages = languages;
        return this;
    }

    public UiExperimentBuilder withHosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public UiExperimentBuilder withIncludeGuids(List<String> guids) {
        this.includeGuids = guids;
        return this;
    }

    public UiExperimentBuilder withExcludeGuids(List<String> guids) {
        this.excludeGuids = guids;
        return this;
    }

    public UiExperimentBuilder withWixUsers(boolean wixUsers) {
        this.wixUsers = wixUsers;
        return this;
    }

    public UiExperimentBuilder withAllRegistered(boolean allRegistered) {
        this.allRegistered = allRegistered;
        return this;
    }

    public UiExperimentBuilder withNonRegistered(boolean nonRegistered) {
        this.nonRegistered = nonRegistered;
        return this;
    }

    public UiExperimentBuilder withNewRegistered(boolean newRegistered) {
        this.newRegistered = newRegistered;
        return this;
    }

    public UiExperimentBuilder withAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public UiExperimentBuilder withParentStartTime(long parentStartTime) {
        this.parentStartTime = parentStartTime;
        return this;
    }

    public UiExperimentBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public UiExperimentBuilder withUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public UiExperimentBuilder withIncludeUserAgentRegexes(List<String> includeUserAgentRegexes) {
        this.includeUserAgentRegexes = includeUserAgentRegexes;
        return this;
    }

    public UiExperimentBuilder withExcludeUserAgentRegexes(List<String> excludeUserAgentRegexes) {
        this.excludeUserAgentRegexes = excludeUserAgentRegexes;
        return this;
    }

    public UiExperimentBuilder withMetaSiteIds(List<String> guids) {
        this.metaSiteIds = guids;
        return this;
    }

    public UiExperimentBuilder withExcludeMetaSiteIds(boolean isExcludeMetaSite) {
        this.isExcludeMetaSite = isExcludeMetaSite;
        return this;
    }

    public UiExperimentBuilder withExcludeUserGroups(List<String> excludeUserGroups) {
        this.excludeUserGroups = excludeUserGroups;
        return this;
    }

    public static UiExperimentBuilder aCopyOf(UiExperiment uiExperiment) {
        return anUiExperiment().
                withid(uiExperiment.getId()).
                withOriginalId(uiExperiment.getOriginalId()).
                withLinkId(uiExperiment.getLinkId()).
                withName(uiExperiment.getName()).
                withType(uiExperiment.getType()).
                withCreator(uiExperiment.getCreator()).
                withScope(uiExperiment.getScope()).
                withState(uiExperiment.getState()).
                withLastUpdated(uiExperiment.getLastUpdated()).
                withKey(uiExperiment.getKey()).
                withSpecKey(uiExperiment.isSpecKey()).
                withCreationDate(uiExperiment.getCreationDate()).
                withDescription(uiExperiment.getDescription()).
                withComment(uiExperiment.getComment()).
                withUpdater(uiExperiment.getUpdater()).
                withStartDate(uiExperiment.getStartDate()).
                withEndDate(uiExperiment.getEndDate()).
                withGroups(uiExperiment.getGroups()).
                withEditable(uiExperiment.isEditable()).
                withPaused(uiExperiment.isPaused()).
                withGeo(uiExperiment.getGeo()).
                withExcludeGeo(uiExperiment.isExcludeGeo()).
                withLanguages(uiExperiment.getLanguages()).
                withHosts(uiExperiment.getHosts()).
                withIncludeGuids(uiExperiment.getIncludeGuids()).
                withExcludeGuids(uiExperiment.getExcludeGuids()).
                withWixUsers(uiExperiment.isWixUsers()).
                withAllRegistered(uiExperiment.isAllRegistered()).
                withNewRegistered(uiExperiment.isNewRegistered()).
                withNonRegistered(uiExperiment.isNonRegistered()).
                withAnonymous(uiExperiment.isAnonymous()).
                withParentStartTime(uiExperiment.getParentStartTime()).
                withIncludeUserAgentRegexes(uiExperiment.getIncludeUserAgentRegexes()).
                withExcludeUserAgentRegexes(uiExperiment.getExcludeUserAgentRegexes()).
                withMetaSiteIds(uiExperiment.getMetaSiteIds()).
                withExcludeMetaSiteIds(uiExperiment.isExcludeMetaSiteIds())
                .withConductLimit(uiExperiment.getConductLimit())
                .withExcludeUserGroups(uiExperiment.getExcludeUserGroups());
    }


    public UiExperiment build() {
        UiExperiment uiExperiment = new UiExperiment(id, updater, originalId, linkId, name, type, creator, scope, lastUpdated, key, specKey, creationDate, description, comment, startDate, endDate, groups, state, paused, parentStartTime, editable, wixUsers, allRegistered, newRegistered, anonymous, includeGuids, excludeGuids, isExcludeGeo, geo, languages, hosts, includeUserAgentRegexes, excludeUserAgentRegexes, isExcludeMetaSite, metaSiteIds, conductLimit, nonRegistered, excludeUserGroups);
        return uiExperiment;
    }
}
