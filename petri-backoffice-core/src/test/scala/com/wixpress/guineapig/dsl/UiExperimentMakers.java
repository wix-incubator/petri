package com.wixpress.guineapig.dsl;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.wixpress.guineapig.entities.ui.ExperimentType;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.entities.ui.UiExperimentBuilder;
import com.wixpress.guineapig.entities.ui.UiTestGroup;

import java.util.ArrayList;
import java.util.List;

import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * Date: 12/19/13
 * Time: 10:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class UiExperimentMakers {

    public static final Property<UiExperiment, Boolean> wixUsers = newProperty();
    public static final Property<UiExperiment, Boolean> allRegistered = newProperty();
    public static final Property<UiExperiment, Boolean> newRegistered = newProperty();
    public static final Property<UiExperiment, Boolean> nonRegistered = newProperty();
    public static final Property<UiExperiment, Boolean> anonymous = newProperty();
    public static final Property<UiExperiment, Boolean> excludeGeo = newProperty();
    public static final Property<UiExperiment, List<String>> geo = newProperty();
    public static final Property<UiExperiment, List<String>> includeUserAgentRegexes = newProperty();
    public static final Property<UiExperiment, List<String>> excludeUserAgentRegexes = newProperty();
    public static final Property<UiExperiment, List<String>> hosts = newProperty();
    public static final Property<UiExperiment, List<String>> excludeGuids = newProperty();
    public static final Property<UiExperiment, List<String>> includeGuids = newProperty();
    public static final Property<UiExperiment, Boolean> excludeMetaSiteIds = newProperty();
    public static final Property<UiExperiment, List<String>> metaSiteIds = newProperty();
    public static final Property<UiExperiment, String> scope = newProperty();
    public static final Property<UiExperiment, List<UiTestGroup>> groups = newProperty();
    public static final Property<UiExperiment, String> type = newProperty();
    public static final Property<UiExperiment, Integer> conductionLimit = newProperty();
    public static final Property<UiExperiment, Boolean> specKey = newProperty();
    public static final Property<UiExperiment, List<String>> excludeUserGroups = newProperty();


    public static final Instantiator<UiExperiment> UiExperiment = new Instantiator<UiExperiment>() {
        @Override
        public UiExperiment instantiate(PropertyLookup<UiExperiment> lookup) {
            return UiExperimentBuilder.anUiExperiment()
                    .withSpecKey(lookup.valueOf(specKey, true))
                    .withType(lookup.valueOf(type, ExperimentType.AB_TESTING.getType()))
                    .withScope(lookup.valueOf(scope, ""))
                    .withGeo(lookup.valueOf(geo, new ArrayList<String>()))
                    .withIncludeUserAgentRegexes(lookup.valueOf(includeUserAgentRegexes, new ArrayList<String>()))
                    .withExcludeUserAgentRegexes(lookup.valueOf(excludeUserAgentRegexes, new ArrayList<String>()))
                    .withExcludeGeo(lookup.valueOf(excludeGeo, false))
                    .withWixUsers(lookup.valueOf(wixUsers, false))
                    .withAllRegistered(lookup.valueOf(allRegistered, false))
                    .withNewRegistered(lookup. valueOf(newRegistered, false))
                    .withNonRegistered(lookup. valueOf(nonRegistered, false))
                    .withAnonymous(lookup.valueOf(anonymous, false))
                    .withGroups(lookup.valueOf(groups, TestGroupMakers.DEFAULT_UI_TEST_GROUPS))
                    .withExcludeGuids(lookup.valueOf(excludeGuids, new ArrayList<String>()))
                    .withIncludeGuids(lookup.valueOf(includeGuids, new ArrayList<String>()))
                    .withExcludeMetaSiteIds(lookup.valueOf(excludeMetaSiteIds, false))
                    .withMetaSiteIds(lookup.valueOf(metaSiteIds, new ArrayList<String>()))
                    .withHosts(lookup.valueOf(hosts, new ArrayList<String>()))
                    .withConductLimit(lookup.valueOf(conductionLimit, 0))
                    .withExcludeUserGroups(lookup.valueOf(excludeUserGroups, new ArrayList<String>()))
                    .build();
        }
    };
}