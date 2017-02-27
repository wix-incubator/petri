package com.wixpress.petri.petri;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Multimaps.index;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.HasID.hasID;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsEndedAutomaticallyInInterval.isEndedAutomaticallyInInterval;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsActivePredicate.isActiveNow;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 1/15/14
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RAMPetriClient implements FullPetriClient, PetriClient, UserRequestPetriClient {

    private Map<ExperimentKey, Experiment> experiments = new LinkedHashMap<>();
    private int currentId = 1;
    private Map<String, ExperimentSpec> specs = new TreeMap<String, ExperimentSpec>(String.CASE_INSENSITIVE_ORDER);
    private boolean blowUp = false;
    private List<ConductExperimentReport> reports = new ArrayList<>();
    private Map<UUID, String> userStateMap = new HashMap<>();

    public synchronized void clearAll() {
        experiments.clear();
        specs.clear();
        currentId = 1;
        reports.clear();
        userStateMap.clear();
    }

    public void setBlowUp(boolean blowUp) {
        this.blowUp = blowUp;
    }


    private class ExperimentKey {
        public final DateTime date;
        public final int id;

        private ExperimentKey(DateTime date, int id) {
            this.date = date;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExperimentKey that = (ExperimentKey) o;

            if (id != that.id) return false;
            if (!date.equals(that.date)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = date.hashCode();
            result = 31 * result + id;
            return result;
        }
    }

    @Override
    public synchronized List<Experiment> fetchActiveExperiments() {
        if (blowUp) {
            throw new FetchActiveExperimentsException("");
        }
        return newArrayList(filter(fetchAllExperiments(), isActiveNow()));
    }


    private Function<? super Collection<Experiment>, ? extends Experiment> mostRecent() {
        return new Function<Collection<Experiment>, Experiment>() {
            @Nullable
            @Override
            public Experiment apply(@Nullable Collection<Experiment> experiments) {
                return getLast(experiments);
            }
        };
    }

    private Function<Experiment, Integer> experimentId() {
        return new Function<Experiment, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable Experiment experiment) {
                return experiment.getId();
            }
        };
    }

    private Function<Experiment, Integer> originalId() {
        return new Function<Experiment, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable Experiment experiment) {
                return experiment.getOriginalId();
            }
        };
    }

    @Override
    public synchronized List<Experiment> fetchAllExperiments() {
        final ImmutableListMultimap<Integer, Experiment> groupedById = index(experiments.values(), experimentId());
        return newArrayList(transform(groupedById.asMap().values(), mostRecent()));
    }

    @Override
    public List<Experiment> searchExperiments(SearchParameters parameters) {
        List<Experiment> allExperiments = fetchAllExperiments();
        return allExperiments.stream()
                .filter(experiment -> experiment.toString().contains(parameters.query()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Experiment> fetchAllExperimentsGroupedByOriginalId() {
        List<Experiment> allExperiments = fetchAllExperiments();
        final ImmutableListMultimap<Integer, Experiment> groupedByOriginalId = index(allExperiments, originalId());
        return newArrayList(transform(groupedByOriginalId.asMap().values(), mostRecent()));
    }

    @Override
    public Experiment fetchExperimentById(int experimentId) {
        List<Experiment> allExperiments = fetchAllExperimentsGroupedByOriginalId();
        return find(allExperiments, hasID(experimentId), null);
    }

    @Override
    public synchronized Experiment insertExperiment(ExperimentSnapshot snapshot) {
        if (snapshot.isFromSpec()) {
            assertHasMatchingSpec(snapshot);
        }
        final Experiment newExperiment = anExperiment().
                withId(nextID()).
                withExperimentSnapshot(snapshot).
                withLastUpdated(snapshot.creationDate()).
                build();

        store(newExperiment);
        return newExperiment;
    }

    @Override
    public List<Experiment> fetchRecentlyEndedExperimentsDueToEndDate(int minutesEnded) {
        DateTime now = new DateTime();
        List<Experiment> allExperiments = fetchAllExperiments();
        return newLinkedList(filter(allExperiments, isEndedAutomaticallyInInterval(now.minusMinutes(minutesEnded), now)));
    }

    private int nextID() {
        return currentId++;
    }

    private void assertHasMatchingSpec(ExperimentSnapshot snapshot) {
        if (!specs.containsKey(snapshot.key()))
            throw new CreateFailed(new CreateFailedData(ExperimentSnapshot.class.getSimpleName(), snapshot.key() + ". You cannot add an experiment without adding a matching spec first"));
    }

    private void store(Experiment theExperiment) {
        experiments.put(new ExperimentKey(theExperiment.getLastUpdated(), theExperiment.getId()), theExperiment);
    }

    @Override
    public synchronized Experiment updateExperiment(Experiment experiment) {
        if (experiment.isFromSpec()) {
            assertHasMatchingSpec(experiment);
        }
        assertExistsExperiment(experiment);
        Experiment updated = aCopyOf(experiment)
                .withLastUpdated(now()).build();
        store(updated);
        return updated;
    }

    private DateTime now() {
        return new DateTime();
    }

    private void assertExistsExperiment(Experiment experiment) {
        if (!isExistingExperiment(experiment))
            throw new FullPetriClient.UpdateFailed(experiment);
    }

    private void assertHasMatchingSpec(Experiment experiment) {
        if (!hasMatchingSpec(experiment))
            throw new FullPetriClient.UpdateFailed(experiment);
    }

    private boolean hasMatchingSpec(Experiment experiment) {
        return specs.containsKey(experiment.getKey());
    }

    private boolean isExistingExperiment(Experiment experiment) {
        return getHistoryById(experiment.getId()).size() != 0;
    }

    @Override
    public synchronized List<ExperimentSpec> fetchSpecs() {
        return new ArrayList<>(specs.values());
    }

    @Override
    public synchronized void addSpecs(List<ExperimentSpec> expectedSpecs) {
        for (ExperimentSpec expectedSpec : expectedSpecs) {
            store(expectedSpec);
        }
    }

    @Override
    public void reportConductExperiment(List<ConductExperimentReport> conductExperimentReports) {
        store(conductExperimentReports);

    }

    @Override
    public void saveUserState(UUID userId, String userState) {
        userStateMap.put(userId, userState);
    }


    @Override
    public String getUserState(UUID userId) {
        if (userStateMap.containsKey(userId))
            return userStateMap.get(userId);
        else return "";
    }

    public boolean isUserStateDefined(UUID userId) {
        return userStateMap.containsKey(userId);
    }

    @Override
    public synchronized List<Experiment> getHistoryById(final int id) {
        return reverse(newLinkedList(filter(experiments.values(), hasID(id))));
    }

    @Override
    public synchronized void deleteSpec(String key) {
        specs.remove(key);
    }


    @Override
    public List<ConductExperimentSummary> getExperimentReport(int experimentId) {

        ConductExperimentReport report = getConductExperimentReport(experimentId);
        return ImmutableList.of(new ConductExperimentSummary(report.serverName(), report.experimentId(), report.experimentValue(),
                report.count(), report.count(), new DateTime()));
    }

    @Override
    public List<Integer> getLatestExperimentReportsAfterUpdateDate(DateTime lastUpdateDate) {
        return newArrayList(transform(reports, convertReportToExperimentId()));
    }



    private Function<ConductExperimentReport, Integer> convertReportToExperimentId() {
        return new Function<ConductExperimentReport, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable ConductExperimentReport report) {
                if (report != null) {
                    return report.experimentId();
                } else throw new IllegalArgumentException("report must not be null");
            }
        };
    }


    private void store(List<ConductExperimentReport> conductExperimentReports) {
        reports.addAll(conductExperimentReports);
    }

    public ConductExperimentReport getConductExperimentReport(int id) {
        for (ConductExperimentReport report : reports) {
            if (report.experimentId() == id)
                return report;
        }
        return null;
    }

    private void store(ExperimentSpec expectedSpec) {
        specs.put(expectedSpec.getKey(), expectedSpec);
    }

    class FetchActiveExperimentsException extends RuntimeException{
        public FetchActiveExperimentsException(String message) {
            super(message);
        }
    }

}
