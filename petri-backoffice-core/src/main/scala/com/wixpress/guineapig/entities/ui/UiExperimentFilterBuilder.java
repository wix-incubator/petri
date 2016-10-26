package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.wixpress.guineapig.services.UnrecognizedFilterException;
import com.wixpress.guineapig.spi.FilterAdapterExtender;
import com.wixpress.petri.experiments.domain.*;

import java.util.*;

import static com.google.common.collect.Lists.transform;
import static com.wixpress.petri.experiments.domain.AggregateFilter.IsNotFilter.isNotFilter;


public class UiExperimentFilterBuilder {

    Map<FilterType, Filter> filters = new HashMap<>();

    public UiExperimentFilterBuilder(List<Filter> filters, FilterAdapterExtender filterAdapterExtender) throws JsonProcessingException, ClassNotFoundException {

        List<Filter> unrecognized = new ArrayList<>();

        for (Filter filter : filters) {
            FilterType filterName = getFilterName(filter);
            if (filterName != null) {
                this.filters.put(filterName, filter);
            }
            else{
                unrecognized.add(filter);
            }
        }

        //TODO - TEST + now that we can refactor move this whole class to scala!
        for (Filter unrecognizedFilter : unrecognized) {
            if (!filterAdapterExtender.isRecognized(unrecognizedFilter)) {
                String unrecognizedClass = unrecognizedFilter.getClass().getSimpleName();
                if (unrecognizedFilter.getClass() == UnrecognizedFilter.class) {
                    throw new UnrecognizedFilterException(unrecognizedClass);
                }
                throw new ClassNotFoundException(unrecognizedClass);
            }
        }

    }

    //TODO : test!!!

    public boolean isWixUsers() {
        return filters.containsKey(FilterType.WIX_USERS);
    }

    public String getUsers() {
        if (filters.containsKey(FilterType.NEW_USERS)) {
            return FilterType.NEW_USERS.getType();
        }
        if (filters.containsKey(FilterType.FIRST_TIME_ANON_USERS)) {
            return FilterType.FIRST_TIME_ANON_USERS.getType();
        }
        if (filters.containsKey(FilterType.REGISTERED_USERS)) {
            return FilterType.REGISTERED_USERS.getType();
        }
        if (filters.containsKey(FilterType.NON_REGISTERED_USERS)) {
            return FilterType.NON_REGISTERED_USERS.getType();
        }

        return FilterType.USERS_NONE.getType();
    }

    public UiGeoFilter getGeo() {
        if (filters.containsKey(FilterType.EXCLUDE_GEO)) {
            return UiGeoFilter.create(true, ((GeoFilter) ((NotFilter) filters.get(FilterType.EXCLUDE_GEO)).getInternal()).getCountries());
        }
        if (filters.containsKey(FilterType.INCLUDE_GEO)) {
            return UiGeoFilter.create(false, ((GeoFilter) this.filters.get(FilterType.INCLUDE_GEO)).getCountries());

        }
        return UiGeoFilter.create(false, new ArrayList<String>());
    }

    public UiUserAgentRegexFilter getUserAgentRegex() {
        if (filters.containsKey(FilterType.USER_AGENT_REGEX)) {
            UserAgentRegexFilter userAgentRegexFilter = (UserAgentRegexFilter) this.filters.get(FilterType.USER_AGENT_REGEX);
            return UiUserAgentRegexFilter.create(userAgentRegexFilter.includeUserAgentRegexes(),
                    userAgentRegexFilter.excludeUserAgentRegexes());
        }
        return UiUserAgentRegexFilter.create(new ArrayList<String>(), new ArrayList<String>());
    }

    public UiUserNotInGroupFilter getUserNotInGroup() {
        if (filters.containsKey(FilterType.USER_NOT_IN_ANY_GROUP)) {
            UserNotInAnyGroupFilter userNotInAnyGroupFilter = (UserNotInAnyGroupFilter) this.filters.get(FilterType.USER_NOT_IN_ANY_GROUP);
            return UiUserNotInGroupFilter.create(userNotInAnyGroupFilter.excludeUserGroups());
        }
        return UiUserNotInGroupFilter.create(ImmutableList.of());
    }

    public List<String> getLanguages() {
        if (filters.containsKey(FilterType.LANGUAGE)) {
            return ((LanguageFilter) this.filters.get(FilterType.LANGUAGE)).getLanguages();
        }
        return new ArrayList<>();
    }

    public List<String> getHosts() {
        if (filters.containsKey(FilterType.HOST)) {
            return ((HostFilter) this.filters.get(FilterType.HOST)).getHosts();
        }
        return new ArrayList<>();
    }

    public List<String> getArtifacts() {
        if (filters.containsKey(FilterType.ARTIFACT)) {
            return ((ArtifactFilter) this.filters.get(FilterType.ARTIFACT)).getArtifacts();
        }
        return new ArrayList<>();
    }

    public List<String> getIncludeGuids() {
        List<String> ids = new ArrayList<>();
        if (filters.containsKey(FilterType.INCLUDE_ID)) {
            List<UUID> uuids = ((IncludeUserIdsFilter) filters.get(FilterType.INCLUDE_ID)).getIds();
            ids = transform(uuids, guidToString());
        }
        return ids;
    }

    public List<String> getExcludeGuids() {
        List<String> ids = new ArrayList<>();
        if (filters.containsKey(FilterType.EXCLUDE_ID)) {
            List<UUID> uuids = ((IncludeUserIdsFilter) ((NotFilter) filters.get(FilterType.EXCLUDE_ID)).getInternal()).getIds();
            ids = transform(uuids, guidToString());
        }
        return ids;
    }

    private FilterType getFilterName(Filter filter){
        Class c = filter.getClass();
        if (isNotFilter(GeoFilter.class).apply(filter)) {
            return FilterType.EXCLUDE_GEO;

        } else if (filter.getClass() == GeoFilter.class) {
            return FilterType.INCLUDE_GEO;
        }
        if (filter.getClass() == LanguageFilter.class) {
            return FilterType.LANGUAGE;
        }
        if (filter.getClass() == HostFilter.class) {
            return FilterType.HOST;
        }
        if (filter.getClass() == ArtifactFilter.class) {
            return FilterType.ARTIFACT;
        }
        if (filter.getClass() == WixEmployeesFilter.class) {
            return FilterType.WIX_USERS;
        }
        if (filter.getClass() == RegisteredUsersFilter.class) {
            return FilterType.REGISTERED_USERS;
        }
        if (filter.getClass() == NonRegisteredUsersFilter.class) {
            return FilterType.NON_REGISTERED_USERS;
        }
        if (filter.getClass() == NewUsersFilter.class) {
            return FilterType.NEW_USERS;
        }
        if (filter.getClass() == FirstTimeVisitorsOnlyFilter.class) {
            return FilterType.FIRST_TIME_ANON_USERS;
        }
        if (filter.getClass() == IncludeUserIdsFilter.class) {
            return FilterType.INCLUDE_ID;
        }
        if (isNotFilter(IncludeUserIdsFilter.class).apply(filter)) {
            return FilterType.EXCLUDE_ID;
        }
        if (filter.getClass() == UserAgentRegexFilter.class) {
            return FilterType.USER_AGENT_REGEX;
        }
        if (filter.getClass() == UserNotInAnyGroupFilter.class) {
            return FilterType.USER_NOT_IN_ANY_GROUP;
        }
        return null;
    }


    private static Function<UUID, String> guidToString() {
        return new Function<UUID, String>() {
            @Override
            public String apply(UUID guid) {
                return guid.toString();
            }
        };
    }

    public boolean isOpenToAll() {
        return this.filters.size() == 0;
    }


}
