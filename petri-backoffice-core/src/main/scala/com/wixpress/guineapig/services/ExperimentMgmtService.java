package com.wixpress.guineapig.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.wixpress.guineapig.entities.ui.ExperimentReport;
import com.wixpress.guineapig.spi.HardCodedScopesProvider;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.Clock;
import com.wixpress.petri.petri.FullPetriClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.find;
import static com.wixpress.guineapig.entities.ui.ExperimentReportBuilder.buildReport;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.SpecHasKey.specHasKey;

public class ExperimentMgmtService implements GuineapigExperimentMgmtService {
    final EventPublisher experimentEventPublisher;
    final Clock clock;
    private final FullPetriClient fullPetriClient;
    private final HardCodedScopesProvider hardCodedScopesProvider;
    private Logger log = LoggerFactory.getLogger(ExperimentMgmtService.class);

    public ExperimentMgmtService(Clock clock, EventPublisher experimentEventPublisher, FullPetriClient fullPetriClient, HardCodedScopesProvider hardCodedScopesProvider) {
        this.clock = clock;
        this.experimentEventPublisher = experimentEventPublisher;
        this.fullPetriClient = fullPetriClient;
        this.hardCodedScopesProvider = hardCodedScopesProvider;
    }

    @Override
    public ExperimentReport getExperimentReport(int experimentId) {
        return buildReport(experimentId, fullPetriClient.getExperimentReport(experimentId));
    }

    @Override
    public boolean pauseExperiment(int experimentId, String comment, String userName) {
        Experiment experiment = fullPetriClient.fetchExperimentById(experimentId);
        Experiment pausedExperiment = experiment.pause(new Trigger(comment, userName));
        pausedExperiment = fullPetriClient.updateExperiment(pausedExperiment);
        experimentEventPublisher.publish(new ExperimentEvent(pausedExperiment, null, ExperimentEvent.PAUSED));
        return true;
    }

    @Override
    public boolean resumeExperiment(int experimentId, String comment, String userName) {
        Experiment experiment = fullPetriClient.fetchExperimentById(experimentId);
        Experiment resumedExperiment = experiment.resume(new Trigger(comment, userName));
        resumedExperiment = fullPetriClient.updateExperiment(resumedExperiment);
        experimentEventPublisher.publish(new ExperimentEvent(resumedExperiment, null, ExperimentEvent.RESUMED));
        return true;
    }

    @Override
    public List<Experiment> getAllExperiments() throws JsonProcessingException, ClassNotFoundException {
        log.info("getallExperiments...");
        List<Experiment> allExperiments = fullPetriClient.fetchAllExperimentsGroupedByOriginalId();
        log.info("getallExperiments !!!");
        return allExperiments;
    }

    // TODO: Throw exception here or use Option (instead of returning null)?
    @Override
    public ExperimentSpec getSpecForExperiment(String experimentKey) {
        return find(fullPetriClient.fetchSpecs(), specHasKey(experimentKey), null);
    }

    @Override
    public boolean updateExperiment(Experiment updatedExperiment, String userName) throws IOException, IllegalArgumentException {
        Experiment currentVersion = fullPetriClient.fetchExperimentById(updatedExperiment.getId());

        if (!containsValidFilters(updatedExperiment)) {
            throw new IllegalArgumentException("Invalid filters for scope " + updatedExperiment.getScope() + ".");
        }


        if (expandIsNeeded(updatedExperiment, currentVersion)) {
            DateTime terminationTime = clock.getCurrentDateTime();
            boolean shouldTerminateChain = currentVersion.canRelyOnPreviouslyConductedUid(updatedExperiment.getFilters());
            fullPetriClient.updateExperiment(currentVersion.pauseOrTerminateAsOf(terminationTime,
                    updatedExperiment.getFilters(),
                    new Trigger(updatedExperiment.getComment(), userName)));

            updatedExperiment = fullPetriClient.insertExperiment(updatedSnapshotAsNewInstance(updatedExperiment, currentVersion, terminationTime).build());
            experimentEventPublisher.publish(new ExperimentEvent(updatedExperiment, currentVersion, ExperimentEvent.EXPANDED));
            if (shouldTerminateChain)
                terminateHistoryIfNeeded(updatedExperiment, userName, currentVersion, terminationTime);
        } else {
            updatedExperiment = fullPetriClient.updateExperiment(updatedExperiment);
            experimentEventPublisher.publish(new ExperimentEvent(updatedExperiment, currentVersion, ExperimentEvent.UPDATED));
        }

        return true;
    }

    private void terminateHistoryIfNeeded(final Experiment finalUpdatedExperiment, String userName, Experiment currentVersion, DateTime terminationTime) {
        getExperimentsByOriginalId(finalUpdatedExperiment)
                .filter((experiment) -> experiment.getId() != currentVersion.getId() && experiment.getId() != finalUpdatedExperiment.getId())
                .forEach((experiment) -> {
                    if (experiment.isPaused()) {
                        fullPetriClient.updateExperiment(experiment.terminateAsOf(terminationTime, new Trigger(finalUpdatedExperiment.getComment(), userName)));
                    }
                });
    }

    @Override
    public Experiment getExperimentById(int experimentId) {
        log.info("getExperiment with id = " + experimentId);
        try {

            List<Experiment> all = fullPetriClient.fetchAllExperimentsGroupedByOriginalId();
            for (Experiment experiment : all) {
                if (experiment.getId() == experimentId) {
                    log.info("getExperiment with id = " + experimentId + " found !!!");
                    return experiment;
                }
            }
        } catch (Exception e) {
        }

        log.error("getExperiment with id = " + experimentId + " not found !!!");
        return null;
    }

    @Override
    public List<Experiment> getHistoryById(int experimentId) {
        log.info("getHistoryById...");
        List<Experiment> history = fullPetriClient.getHistoryById(experimentId);
        log.info("getHistoryById !!!");
        return history;
    }

    @Override
    public boolean newExperiment(ExperimentSnapshot snapshot) throws IOException, IllegalArgumentException {
        if (!containsValidFilters(snapshot))
            throw new IllegalArgumentException("Invalid filters for scopes " + snapshot.scopes().stream().reduce("", (a, b) -> a + "," + b) + ".");

        Experiment insertedExperiment = fullPetriClient.insertExperiment(snapshot);
        experimentEventPublisher.publish(new ExperimentEvent(insertedExperiment, null, ExperimentEvent.CREATED));
        return true;
    }

    @Override
    public boolean terminateExperiment(int experimentId, String comment, String userName) {
        log.info("terminateExperiment with id = " + experimentId);
        final Experiment originalExperiment = fullPetriClient.fetchExperimentById(experimentId);
        Experiment experiment = originalExperiment;

        List<Experiment> experimentsByOriginalId = getExperimentsByOriginalId(originalExperiment).collect(Collectors.toList());

        for (Experiment experimentWithOriginalId : experimentsByOriginalId) {
            if (!experimentWithOriginalId.isTerminated()) {
                Experiment terminatedExperiment = experimentWithOriginalId.terminateAsOf(clock.getCurrentDateTime(),
                        new Trigger(comment, userName));
                experiment = fullPetriClient.updateExperiment(terminatedExperiment);
            }
        }

        experimentEventPublisher.publish(new ExperimentEvent(experiment, null, ExperimentEvent.TERMINATED));
        return true;
    }

    private Stream<Experiment> getExperimentsByOriginalId(Experiment originalExperiment) {
        return fullPetriClient.fetchAllExperiments().stream().filter(exp ->
                exp.getOriginalId() == originalExperiment.getOriginalId());
    }

    private ExperimentSpec getSpecForExperiment(ExperimentSnapshot experimentSnapshot) {
        List<ExperimentSpec> specs = fullPetriClient.fetchSpecs();
        return Iterables.find(specs, spec -> experimentSnapshot.key().equals(spec.getKey()));
    }

    protected boolean containsValidFilters(ExperimentSnapshot experimentSnapshot) {

        List<ScopeDefinition> possibleScopes = new ArrayList<>();
        possibleScopes.addAll(hardCodedScopesProvider.getHardCodedScopesList());

        if (experimentSnapshot.isFromSpec()) {
            ExperimentSpec spec = getSpecForExperiment(experimentSnapshot);
            List<ScopeDefinition> scopesFromSpec = spec.getScopes();
            possibleScopes.addAll(scopesFromSpec);
        }
        List<ScopeDefinition> scopeDefinitions = possibleScopes.stream().filter(possibleScope -> experimentSnapshot.scopes().stream().anyMatch(scope -> scope.equals(possibleScope.getName()))).collect(Collectors.toList());

        return scopeDefinitions.stream().allMatch(scopeDefinition -> isValidFiltersForScope(scopeDefinition, experimentSnapshot.filters()));
    }

    private boolean isValidFiltersForScope(ScopeDefinition scope, List<Filter> filters) {
        if (scope.isOnlyForLoggedInUsers() &&
                (
                        filters.contains(new FirstTimeVisitorsOnlyFilter()) ||
                                filters.contains(new NonRegisteredUsersFilter()))
                )
            return false;

        else if (!scope.isOnlyForLoggedInUsers() &&
                (
                        filters.contains(new WixEmployeesFilter()) ||
                                filters.contains(new IncludeUserIdsFilter()) ||
                                filters.contains(new NotFilter(new IncludeUserIdsFilter())) ||
                                filters.contains(new NewUsersFilter()))
                )
            return false;

        return true;
    }

    private ExperimentSnapshotBuilder updatedSnapshotAsNewInstance(Experiment updatedExperiment, Experiment currentVersion, DateTime terminationTime) {
        ExperimentSnapshotBuilder updatedSnapshotWithCopiedOriginalId = ExperimentSnapshotBuilder.aCopyOf(
                updatedExperiment.getExperimentSnapshot()).
                withOriginalId(currentVersion.getOriginalId()).
                withCreationDate(clock.getCurrentDateTime());

        if (currentVersion.isActiveAt(terminationTime) && updatedExperiment.getStartDate().isEqual(currentVersion.getStartDate())) {
            updatedSnapshotWithCopiedOriginalId = updatedSnapshotWithCopiedOriginalId.withStartDate(terminationTime);
        }

        return updatedSnapshotWithCopiedOriginalId;
    }

    private boolean expandIsNeeded(Experiment experimentUpdate, Experiment latestVersion) {
        return (!experimentUpdate.isToggle() && !latestVersion.isToggle()) &&
                (!experimentUpdate.getFilters().equals(latestVersion.getFilters()) ||
                        !experimentUpdate.getGroups().equals(latestVersion.getGroups()));
    }

    private boolean containsValidFilters(Experiment experiment) {
        return containsValidFilters(experiment.getExperimentSnapshot());
    }
}
