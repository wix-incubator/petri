package com.wixpress.guineapig.entities.ui;

import com.google.common.base.Function;
import com.wixpress.petri.experiments.domain.*;

import java.util.*;

import static com.google.common.collect.Lists.transform;

public class ExperimentFilterBuilder {

    public static List<Filter> extractFiltersFromUiExperiment(UiExperiment uiExperiment){
        List<Filter> filters = new ArrayList<Filter>();

        if (0 < uiExperiment.getIncludeGuids().size()) {
            List<String> includeGuids = uiExperiment.getIncludeGuids();
            List<UUID> guidList = transform(includeGuids, stringToGuid());

            IncludeUserIdsFilter includeUserIdsFilter = new IncludeUserIdsFilter(guidList.toArray(new UUID[guidList.size()]));
            filters.add(includeUserIdsFilter);
        }

        if (0 < uiExperiment.getExcludeGuids().size()) {
            List<String> excludeGuids =uiExperiment.getExcludeGuids();
            List<UUID> guidList = transform(excludeGuids, stringToGuid());

            IncludeUserIdsFilter userIdsFilter = new IncludeUserIdsFilter(guidList.toArray(new UUID[guidList.size()]));
            filters.add(new NotFilter(userIdsFilter));
        }

        if (uiExperiment.isWixUsers()) {
            filters.add(new WixEmployeesFilter());
        }
        if (uiExperiment.isAnonymous()) {
            filters.add(new FirstTimeVisitorsOnlyFilter());
        }
        if (uiExperiment.isNewRegistered()) {
            filters.add(new NewUsersFilter());
        }
        if (uiExperiment.isNonRegistered()) {
            filters.add(new NonRegisteredUsersFilter());
        }
        if (uiExperiment.isAllRegistered()) {
            filters.add(new RegisteredUsersFilter());
        }

        if (0 < uiExperiment.getGeo().size()) {
            if (uiExperiment.isExcludeGeo()) {
                filters.add(new NotFilter(new GeoFilter(uiExperiment.getGeo())));
            } else {
                filters.add(new GeoFilter(uiExperiment.getGeo()));
            }
        }
        
        if (0 < uiExperiment.getLanguages().size()) {
            filters.add(new LanguageFilter(uiExperiment.getLanguages()));
        }
        if (0 < uiExperiment.getHosts().size()) {
            filters.add(new HostFilter(uiExperiment.getHosts()));
        }
        if (0 < uiExperiment.getIncludeUserAgentRegexes().size() || 0 < uiExperiment.getExcludeUserAgentRegexes().size()) {
            filters.add(new UserAgentRegexFilter(uiExperiment.getIncludeUserAgentRegexes(),
                    uiExperiment.getExcludeUserAgentRegexes()));
        }
        if (uiExperiment.getExcludeUserGroups() != null && 0 < uiExperiment.getExcludeUserGroups().size()) {
            filters.add(new UserNotInAnyGroupFilter(uiExperiment.getExcludeUserGroups()));
        }
        return filters;

    }


    private static Function<String, UUID> stringToGuid() {
        return new Function<String, UUID>() {
            @Override
            public UUID apply(String guid) {
                return UUID.fromString(guid);
            }
        };
    }

}
