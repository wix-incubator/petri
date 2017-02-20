package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;


@JsonDeserialize(builder = UiExperimentBuilder.class)
public class UiExperiment {

    final private String name;
    final private String type;
    final private String creator;
    final private String scope;
    final private String state;
    final private int id;
    final private long lastUpdated;
    final private String key;
    final private boolean specKey;
    final private long creationDate;
    final private String description;


    final private String updater;
    final private String comment;
    final private long startDate;
    final private long endDate;
    final private boolean paused;
    final private List<UiTestGroup> groups;
    final private boolean editable;

    final private boolean wixUsers;

    final private boolean allRegistered;
    final private boolean newRegistered;
    final private boolean nonRegistered;
    final private boolean anonymous;
    final private boolean excludeGeo;
    final private List<String> geo;
    final private List<String> languages;
    final private List<String> hosts;
    final private List<String> includeGuids;
    final private List<String> excludeGuids;
    final private long parentStartTime;
    final private List<String> includeUserAgentRegexes;
    final private List<String> excludeUserAgentRegexes;
    final private List<String> excludeUserGroups;


    final private int originalId;
    final private int linkId;
    final private boolean excludeMetaSiteIds;
    final private List<String> metaSiteIds;

    final private int conductLimit;
    final private boolean forRegisteredUsers;


    UiExperiment(
            Integer id,
            String updater,
            int originalId,
            int linkId, String name,
            String type,
            String creator,
            String scope,
            long lastUpdated,
            String key,
            boolean specKey,
            long creationDate,
            String description,
            String comment,
            long startDate,
            long endDate,
            List<UiTestGroup> groups,
            //state
            String state,
            Boolean paused,

            //filters :
            boolean forRegisteredUsers,
            long parentStartTime,
            boolean editable,
            boolean wixUsers,
            boolean allRegistered,
            boolean newRegistered,
            boolean anonymous,
            List<String> includeGuids,
            List<String> excludeGuids,
            boolean excludeGeo,
            List<String> geo,
            List<String> languages,
            List<String> hosts,
            //editing constrains

            //rules
            List<String> includeUserAgentRegexes,
            List<String> excludeUserAgentRegexes,
            boolean excludeMetaSiteIds,
            List<String> metaSiteIds,
            int conductLimit,
            boolean nonRegistered,
            List<String> excludeUserGroups) {
        this.id = id;
        this.updater = updater;
        this.linkId = linkId;
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.scope = scope;
        this.lastUpdated = lastUpdated;
        this.key = key;
        this.specKey = specKey;
        this.creationDate = creationDate;
        this.description = description;
        this.comment = comment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groups = groups;
        this.state = state;
        this.paused = paused;
        this.allRegistered = allRegistered;
        this.newRegistered = newRegistered;
        this.anonymous = anonymous;
        this.excludeGeo = excludeGeo;
        this.geo = geo;
        this.languages = languages;
        this.hosts = hosts;
        this.includeGuids = includeGuids;
        this.excludeGuids = excludeGuids;
        this.wixUsers = wixUsers;

        this.parentStartTime = parentStartTime;

        this.editable = editable;

        this.originalId = originalId;
        this.includeUserAgentRegexes = includeUserAgentRegexes;
        this.excludeUserAgentRegexes = excludeUserAgentRegexes;
        this.excludeMetaSiteIds = excludeMetaSiteIds;
        this.metaSiteIds = metaSiteIds;
        this.conductLimit = conductLimit;
        this.nonRegistered = nonRegistered;
        this.excludeUserGroups = excludeUserGroups;
        this.forRegisteredUsers = forRegisteredUsers;
    }

    public int getConductLimit() {return conductLimit;}

    public int getLinkId() {
        return linkId;
    }

    public boolean isSpecKey() {
        return specKey;
    }

    public long getParentStartTime() {
        return parentStartTime;
    }


    public boolean isWixUsers() {
        return wixUsers;
    }


    public boolean isAllRegistered() {
        return allRegistered;
    }

    public boolean isNewRegistered() {
        return newRegistered;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public boolean isExcludeGeo() {
        return excludeGeo;
    }

    public List<String> getGeo() {
        return geo;
    }

    public List<String> getIncludeGuids() {
        return includeGuids;
    }

    public List<String> getExcludeGuids() {
        return excludeGuids;
    }


    public boolean isPaused() {
        return paused;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCreator() {
        return creator;
    }

    public String getScope() {
        return scope;
    }

    public String getState() {
        return state;
    }

    public int getId() {
        return id;
    }

    public int getOriginalId() {
        return originalId;
    }

    public String getKey() {
        return key;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getComment() {
        return comment;
    }

    public String getUpdater() {
        return updater;
    }

    public String getDescription() {
        return description;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public boolean isNonRegistered() {
        return nonRegistered;
    }

    public List<UiTestGroup> getGroups() {
        return groups;
    }

    public boolean isExcludeMetaSiteIds() {return excludeMetaSiteIds;}
    
    public List<String> getMetaSiteIds() {
        return metaSiteIds;
    }

    public List<String> getIncludeUserAgentRegexes() {
        return includeUserAgentRegexes;
    }
    public List<String> getExcludeUserAgentRegexes() {
        return excludeUserAgentRegexes;
    }

    public List<String> getExcludeUserGroups() {
        return excludeUserGroups;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isForRegisteredUsers() {
        return forRegisteredUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiExperiment that = (UiExperiment) o;

        if (id != that.id) return false;
        if (lastUpdated != that.lastUpdated) return false;
        if (specKey != that.specKey) return false;
        if (creationDate != that.creationDate) return false;
        if (startDate != that.startDate) return false;
        if (endDate != that.endDate) return false;
        if (paused != that.paused) return false;
        if (editable != that.editable) return false;
        if (wixUsers != that.wixUsers) return false;
        if (allRegistered != that.allRegistered) return false;
        if (newRegistered != that.newRegistered) return false;
        if (nonRegistered != that.nonRegistered) return false;
        if (anonymous != that.anonymous) return false;
        if (excludeGeo != that.excludeGeo) return false;
        if (parentStartTime != that.parentStartTime) return false;
        if (originalId != that.originalId) return false;
        if (linkId != that.linkId) return false;
        if (excludeMetaSiteIds != that.excludeMetaSiteIds) return false;
        if (conductLimit != that.conductLimit) return false;
        if (forRegisteredUsers != that.forRegisteredUsers) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
        if (scope != null ? !scope.equals(that.scope) : that.scope != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (updater != null ? !updater.equals(that.updater) : that.updater != null) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
        if (geo != null ? !geo.equals(that.geo) : that.geo != null) return false;
        if (languages != null ? !languages.equals(that.languages) : that.languages != null) return false;
        if (hosts != null ? !hosts.equals(that.hosts) : that.hosts != null) return false;
        if (includeGuids != null ? !includeGuids.equals(that.includeGuids) : that.includeGuids != null) return false;
        if (excludeGuids != null ? !excludeGuids.equals(that.excludeGuids) : that.excludeGuids != null) return false;
        if (includeUserAgentRegexes != null ? !includeUserAgentRegexes.equals(that.includeUserAgentRegexes) : that.includeUserAgentRegexes != null)
            return false;
        if (excludeUserAgentRegexes != null ? !excludeUserAgentRegexes.equals(that.excludeUserAgentRegexes) : that.excludeUserAgentRegexes != null)
            return false;
        if (excludeUserGroups != null ? !excludeUserGroups.equals(that.excludeUserGroups) : that.excludeUserGroups != null)
            return false;
        return metaSiteIds != null ? metaSiteIds.equals(that.metaSiteIds) : that.metaSiteIds == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (int) (lastUpdated ^ (lastUpdated >>> 32));
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (specKey ? 1 : 0);
        result = 31 * result + (int) (creationDate ^ (creationDate >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (updater != null ? updater.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (endDate ^ (endDate >>> 32));
        result = 31 * result + (paused ? 1 : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (editable ? 1 : 0);
        result = 31 * result + (wixUsers ? 1 : 0);
        result = 31 * result + (allRegistered ? 1 : 0);
        result = 31 * result + (newRegistered ? 1 : 0);
        result = 31 * result + (nonRegistered ? 1 : 0);
        result = 31 * result + (anonymous ? 1 : 0);
        result = 31 * result + (excludeGeo ? 1 : 0);
        result = 31 * result + (geo != null ? geo.hashCode() : 0);
        result = 31 * result + (languages != null ? languages.hashCode() : 0);
        result = 31 * result + (hosts != null ? hosts.hashCode() : 0);
        result = 31 * result + (includeGuids != null ? includeGuids.hashCode() : 0);
        result = 31 * result + (excludeGuids != null ? excludeGuids.hashCode() : 0);
        result = 31 * result + (int) (parentStartTime ^ (parentStartTime >>> 32));
        result = 31 * result + (includeUserAgentRegexes != null ? includeUserAgentRegexes.hashCode() : 0);
        result = 31 * result + (excludeUserAgentRegexes != null ? excludeUserAgentRegexes.hashCode() : 0);
        result = 31 * result + (excludeUserGroups != null ? excludeUserGroups.hashCode() : 0);
        result = 31 * result + originalId;
        result = 31 * result + linkId;
        result = 31 * result + (excludeMetaSiteIds ? 1 : 0);
        result = 31 * result + (metaSiteIds != null ? metaSiteIds.hashCode() : 0);
        result = 31 * result + conductLimit;
        result = 31 * result + (forRegisteredUsers ? 1 : 0);
        return result;
    }
}



