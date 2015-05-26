package com.wixpress.petri.petri;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.wixpress.petri.experiments.domain.*;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.HasKey.hasKey;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsActivePredicate.isActiveAt;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsNotTerminated.isNotTerminated;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.SpecHasKey.specHasKey;
import static com.wixpress.petri.petri.PetriRpcServer.HasSpecSnapshot.hasSpecSnapshot;

/**
 * @author: talyag
 * @since: 9/9/13
 */
public class PetriRpcServer implements FullPetriClient, PetriClient , UserRequestPetriClient, PetriDeveloperApi {
    public static final String SPEC_OWNER_CHANGED_MSG = "Pay attention - Owner of %s has been changed to %s";
    public static final String SPEC_UPDATE_FAILED_MSG = "Failed to update spec [%s]";
    public static final String NON_TERMINATED_EXPERIMENTS_MSG = "Cannot update spec when non-terminated experiments exist on it";

    private final OriginalIDAwarePetriDao<Experiment, ExperimentSnapshot> experimentsDao;
    private final Clock clock;
    private final DeleteEnablingPetriDao<ExperimentSpec, ExperimentSpec> specsDao;
    private final PetriNotifier petriNotifier;
    private final MetricsReportsDao metricsReportsDao;
    private final UserStateDao userStateDao;

    public PetriRpcServer(OriginalIDAwarePetriDao<Experiment, ExperimentSnapshot> experimentsDao, Clock clock,
                          DeleteEnablingPetriDao<ExperimentSpec, ExperimentSpec> specsDao, PetriNotifier petriNotifier,
                          MetricsReportsDao metricsReportsDao, UserStateDao testGroupsDao) {
        this.experimentsDao = experimentsDao;
        this.clock = clock;
        this.specsDao = specsDao;
        this.petriNotifier = petriNotifier;
        this.metricsReportsDao = metricsReportsDao;
        this.userStateDao = testGroupsDao;
    }

    private String printOriginalAndNewSpecs(ExperimentSpec experimentSpec, ExperimentSpec originalSpec) {
        return String.format("Previous spec - [%s], new spec - [%s]", originalSpec, experimentSpec);
    }

    @Override
    public List<Experiment> fetchActiveExperiments() {
        return filterExperiments(and(notNull(), isActiveAt(clock.getCurrentDateTime())));
    }

    @Override
    public List<Experiment> fetchAllExperiments() {
        return filterExperiments(Predicates.<Experiment>notNull());
    }

    @Override
    public List<Experiment> fetchAllExperimentsGroupedByOriginalId() {
        return experimentsDao.fetchAllExperimentsGroupedByOriginalId();
    }

    private List<Experiment> filterExperiments(Predicate<Experiment> predicate) {
        List<Experiment> experiments = experimentsDao.fetch();
        Iterable<Experiment> filtered = filter(experiments, predicate);
        return newArrayList(filtered);
    }

    @Override
    public Experiment insertExperiment(ExperimentSnapshot snapshot) {
        return experimentsDao.add(snapshot);
    }

    @Override
    public Experiment updateExperiment(Experiment experiment) {
        DateTime now = clock.getCurrentDateTime();

        experimentsDao.update(experiment, now);
        return aCopyOf(experiment).withLastUpdated(now).build();
    }

    @Override
    public List<ExperimentSpec> fetchSpecs() {
        return Lists.newArrayList(filter(specsDao.fetch(), notNull()));
    }

    @Override
    public void addSpecs(List<ExperimentSpec> specs) {
        List<ExperimentSpec> existingSpecs = specsDao.fetch();
        for (ExperimentSpec spec : specs) {
            try {
                addSpec(spec, existingSpecs);
            } catch (Exception e) {
                notifyOfFailure(spec, e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Experiment> getHistoryById(int id) {
        return experimentsDao.getHistoryById(id);
    }

    @Override
    public void deleteSpec(String key) {
        if (!hasUnterminatedExperiments(key))
            specsDao.delete(key);
    }

    @Override
    public void reportConductExperiment(List<ConductExperimentReport> conductExperimentReports) {
        metricsReportsDao.addReports(conductExperimentReports);
    }

    @Override
    public void saveUserState(UUID userId, String userState) {
         userStateDao.saveUserState(userId, userState, new DateTime());
    }

    @Override
    public String getUserState(UUID userId) {
        return userStateDao.getUserState(userId);
    }

    @Override
    public List<ConductExperimentSummary> getExperimentReport(int experimentId) {
        return metricsReportsDao.getReport(experimentId);
    }

    private void addSpec(ExperimentSpec experimentSpec, List<ExperimentSpec> existingSpecs) {
        if (specSnapshotExists(experimentSpec, existingSpecs))
            return;

        ExperimentSpec originalSpec = findOriginalSpecByKey(existingSpecs, experimentSpec.getKey());

        if (hasUnterminatedExperiments(experimentSpec.getKey())) {
            notifyOfUpdateFailure(experimentSpec, originalSpec);
            return;
        }

        if (originalSpec == null) {
            addNew(experimentSpec);
        } else {
            updateExisting(experimentSpec, originalSpec);
        }
    }

    private void notifyOfFailure(ExperimentSpec experimentSpec, Exception e) {
        petriNotifier.notify(String.format(SPEC_UPDATE_FAILED_MSG, experimentSpec.getKey()),
                e.toString(),
                experimentSpec.getOwner());
    }

    private void notifyOfUpdateFailure(ExperimentSpec experimentSpec, ExperimentSpec originalSpec) {
        petriNotifier.notify(
                String.format(SPEC_UPDATE_FAILED_MSG, experimentSpec.getKey()) + " - " + NON_TERMINATED_EXPERIMENTS_MSG,
                printOriginalAndNewSpecs(experimentSpec, originalSpec),
                experimentSpec.getOwner());
    }

    private boolean specSnapshotExists(ExperimentSpec experimentSpec, List<ExperimentSpec> existingSpecs) {
        return find(existingSpecs, hasSpecSnapshot(experimentSpec.getExperimentSpecSnapshot()), null) != null;
    }

    private boolean hasUnterminatedExperiments(String specKey) {
        return find(fetchAllExperiments(), and(hasKey(specKey), isNotTerminated()), null) != null;
    }

    private void addNew(ExperimentSpec experimentSpec) {
        specsDao.add(experimentSpec);
    }

    private void updateExisting(ExperimentSpec experimentSpec, ExperimentSpec originalSpec) {
        DateTime now = clock.getCurrentDateTime();
        specsDao.update(experimentSpec.setCreationDate(originalSpec.getCreationDate()), now);

        notifyOwnerIfNeeded(experimentSpec, originalSpec);
    }

    private void notifyOwnerIfNeeded(ExperimentSpec experimentSpec, ExperimentSpec originalSpec) {
        if (!originalSpec.getOwner().equals(experimentSpec.getOwner())) {
            petriNotifier.notify(String.format(SPEC_OWNER_CHANGED_MSG, experimentSpec.getKey(), experimentSpec.getOwner()),
                    printOriginalAndNewSpecs(experimentSpec, originalSpec),
                    originalSpec.getOwner());
        }
    }

    private ExperimentSpec findOriginalSpecByKey(List<ExperimentSpec> existingSpecs, String key) {
        return find(existingSpecs, specHasKey(key), null);
    }

    @Override
    public UserState getFullUserState(UUID userGuid) {
        return userStateDao.getFullUserState(userGuid);
    }


    public static class HasSpecSnapshot implements Predicate<ExperimentSpec> {

        private final ExperimentSpecSnapshot snapshot;

        public HasSpecSnapshot(ExperimentSpecSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public boolean apply(ExperimentSpec input) {
            return input.getExperimentSpecSnapshot().equals(snapshot);
        }

        public static HasSpecSnapshot hasSpecSnapshot(ExperimentSpecSnapshot snapshot) {
            return new HasSpecSnapshot(snapshot);
        }
    }

}
