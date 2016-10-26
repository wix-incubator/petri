package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Predicate;
import com.wixpress.guineapig.spi.FilterAdapterExtender;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 12/17/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentConverter {

    private final Predicate<Experiment> isEditablePredicate;
    private final FilterAdapterExtender filterAdapterExtender;

    public ExperimentConverter(Predicate<Experiment> isEditablePredicate, FilterAdapterExtender filterAdapterExtender) {
        this.isEditablePredicate = isEditablePredicate;
        this.filterAdapterExtender = filterAdapterExtender;
    }

    public UiExperiment convert(Experiment experiment) throws ClassNotFoundException, JsonProcessingException {
        UiExperimentFilterBuilder adapter = new UiExperimentFilterBuilder(experiment.getFilters(), filterAdapterExtender);

        ExperimentState state = calculateExperimentState(experiment);
        boolean editable = isEditablePredicate.apply(experiment);

        List<UiTestGroup> uiTestGroups = new ArrayList<UiTestGroup>();
        for (TestGroup tg : experiment.getGroups()) {
            uiTestGroups.add(UiTestGroup.fromTestGroup(tg));
        }

        UiExperimentBuilder uiExperimentBuilder = UiExperimentBuilder.anUiExperiment()
                .withid(experiment.getId())
                .withOriginalId(experiment.getOriginalId())
                .withLinkId(experiment.getExperimentSnapshot().linkedId())
                .withName(experiment.getName())
                .withCreator(experiment.getCreator())
                .withType(experiment.isToggle() ? ExperimentType.FEATURE_TOGGLE.getType() : ExperimentType.AB_TESTING.getType())
                .withSpecKey(experiment.isFromSpec())
                .withKey(experiment.getKey())
                .withCreationDate(experiment.getCreationDate().getMillis())
                .withDescription(experiment.getDescription())
                .withComment(experiment.getComment())
                .withUpdater(experiment.getUpdater())
                .withStartDate(experiment.getStartDate().getMillis())
                .withEndDate(experiment.getEndDate().getMillis())
                .withGroups(uiTestGroups)
                .withScope(experiment.getScope())
                .withState(state.getState())
                .withPaused(experiment.isPaused())
                .withEditable(editable)
                .withWixUsers(adapter.isWixUsers())
                .withGeo(adapter.getGeo().geo())
                .withExcludeGeo(adapter.getGeo().isExclude())
                .withLanguages(adapter.getLanguages())
                .withHosts(adapter.getHosts())
                .withArtifacts(adapter.getArtifacts())
                .withIncludeGuids(adapter.getIncludeGuids())
                .withExcludeGuids(adapter.getExcludeGuids())
                .withAllRegistered(adapter.getUsers().equals(FilterType.REGISTERED_USERS.getType()))
                .withNonRegistered(adapter.getUsers().equals(FilterType.NON_REGISTERED_USERS.getType()))
                .withNewRegistered(adapter.getUsers().equals(FilterType.NEW_USERS.getType()))
                .withAnonymous(adapter.getUsers().equals(FilterType.FIRST_TIME_ANON_USERS.getType()))
                .withLastUpdated(experiment.getLastUpdated().getMillis())
                .withIncludeUserAgentRegexes(adapter.getUserAgentRegex().includeUserAgentRegexes())
                .withExcludeUserAgentRegexes(adapter.getUserAgentRegex().excludeUserAgentRegexes())
                .withExcludeUserGroups(adapter.getUserNotInGroup().excludeUserGroups())
                .withConductLimit(experiment.getExperimentSnapshot().conductLimit());

        filterAdapterExtender.extendUiExperiment(experiment.getFilters(), uiExperimentBuilder);

        return uiExperimentBuilder.build();
    }

    static public ExperimentState calculateExperimentState(Experiment experiment) {
        if (experiment.isTerminated()) {
            return ExperimentState.ENDED;
        }
        if (experiment.isPaused()) {
            return ExperimentState.PAUSED;
        }
        if (experiment.getStartDate().isAfterNow()) {
            return ExperimentState.FUTURE;
        }
        if (experiment.isActiveAt(new DateTime())) {
            return ExperimentState.ACTIVE;
        }
        return ExperimentState.UNKNOWN;
    }

}
