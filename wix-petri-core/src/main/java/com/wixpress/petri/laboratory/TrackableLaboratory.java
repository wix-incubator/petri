package com.wixpress.petri.laboratory;

import com.google.common.collect.ImmutableMap;
import com.wixpress.petri.experiments.domain.Assignment;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.petri.MetricsReporter;
import com.wixpress.petri.petri.PetriTopology;
import com.wixpress.petri.petri.SpecDefinition;
import com.wixpress.petri.petri.UserRequestPetriClient;
import scala.Option;
import scala.Some;

import java.util.*;

import static java.lang.String.valueOf;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class TrackableLaboratory implements Laboratory {

    private final int maxConductionTimeMillis;
    private final Experiments experiments;
    private final UserInfoStorage userInfoStorage;
    private final TestGroupAssignmentTracker testGroupAssignmentTracker;
    private final ErrorHandler laboratoryErrorHandler;
    private final MetricsReporter metricsReporter;
    private final UserRequestPetriClient petriClient;
    private final PetriTopology petriTopology;

    public TrackableLaboratory(Experiments experiments, TestGroupAssignmentTracker testGroupAssignmentTracker, UserInfoStorage userInfoStorage,
                               ErrorHandler laboratoryErrorHandler, int maxConductionTimeMillis, MetricsReporter metricsReporter,
                               UserRequestPetriClient petriClient, PetriTopology petriTopology) {
        this.experiments = experiments;
        this.testGroupAssignmentTracker = testGroupAssignmentTracker;
        this.userInfoStorage = userInfoStorage;
        this.laboratoryErrorHandler = laboratoryErrorHandler;
        this.maxConductionTimeMillis = maxConductionTimeMillis;
        this.metricsReporter = metricsReporter;
        this.petriClient = petriClient;
        this.petriTopology = petriTopology;
    }

    private UserInfo userInfo() {
        return userInfoStorage.read();
    }

    private void reportExperimentException(String experimentKey, Throwable cause) {
        laboratoryErrorHandler.handle("Unexpected exception while conducting experiment with key - '" + experimentKey + "', due to : " + cause.getMessage(), cause, ExceptionType.ErrorConductingExperiment);
    }

    @Override
    public String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue, ConductionContext context) {
        return conductExperiment(experimentKey.getName(), fallbackValue, new StringConverter(), context);
    }

    @Override
    public String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue) {
        return conductExperiment(experimentKey.getName(), fallbackValue, new StringConverter());
    }

    @Override
    public <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter) {
        return conductExperiment(experimentKey.getName(), fallbackValue, resultConverter, ConductionContextBuilder.newInstance());
    }

    @Override
    public <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter, ConductionContext context) {
        return conductExperiment(experimentKey.getName(), fallbackValue, resultConverter, context);
    }

    @Override
    public <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter) {
        return conductExperiment(key, fallbackValue, resultConverter, ConductionContextBuilder.newInstance());
    }

    @Override
    public <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter, ConductionContext context) {
        try {
            removeExpiredExperiments();
            List<Experiment> experimentsByKey = experiments.findNonExpiredByKey(key);
            ExistingTestGroups existingTestGroups = getExistingTestGroups(experimentsByKey, context);

            for (Experiment experiment : experimentsByKey) {
                T result = calcExperimentValue(resultConverter, experiment, context, existingTestGroups.get(experiment.getId()));
                if (result != null) {
                    return result;
                }
            }

            return fallbackValue;

        } catch (Throwable e) {
            reportExperimentException(key, e);
            return fallbackValue;
        }
    }

    @Override
    public Map<String, String> conductAllInScope(String scope) {
        return conductAllInScope(scope, ConductionContextBuilder.newInstance());
    }

    @Override
    public Map<String, String> conductAllInScope(String scope, ConductionContext context) {
        try {
            removeExpiredExperiments();
            return conductAll(experiments.findNonExpiredByScope(scope), context);
        } catch (Throwable e) {
            laboratoryErrorHandler.handle("Unexpected exception while conducting all experiments for scope - " + scope, e, ExceptionType.ErrorConductingExperiment);
            return new HashMap<>();
        }
    }


    private Map<String, String> conductAll(List<Experiment> experiments, ConductionContext context) {
        StringConverter stringResultConverter = new StringConverter();
        HashMap<String, String> results = new HashMap<>();

        ExistingTestGroups existingTestGroups = getExistingTestGroups(experiments, context);

        for (Experiment experiment : experiments) {
            String key = experiment.getKey();
            if (!results.containsKey(key)) {
                String result = calcExperimentValue(stringResultConverter, experiment, context, existingTestGroups.get(experiment.getId()));
                if (result != null) {
                    results.put(key, result);
                }
            }
        }

        return results;
    }

    private void removeExpiredExperiments() {
        //TODO - make userinfo.removeExpired()
        UserInfo updateUserInfo = userInfo().removeExperimentsWhere(expired()).removeAnonymousExperimentsWhere(expired());
        if (!updateUserInfo.equals(userInfo()))
            userInfoStorage.write(updateUserInfo);
    }

    private ExpiredExperiments expired() {
        return new ExpiredExperiments(experiments);
    }

    private <T> T calcExperimentValue(TestResultConverter<T> resultConverter, Experiment experiment, ConductionContext context, Option<Integer> existingTestGroupID) {
        if (userInfo().isRobot) {
            return null;
        }
        try {
            if (overrideShouldBeUsed(experiment)) {
                return valueFromOverride(resultConverter, experiment.getKey());
            }
            if (experiment.isToggle()) {
                return valueFromConduct(resultConverter, experiment, context);
            }

            if (existingTestGroupID.isDefined()) {
                TestGroup existingTestGroup  =  experiment.getTestGroupById(existingTestGroupID.get());
                return resultConverter.convert(existingTestGroup.getValue());
            } else
                return valueFromConduct(resultConverter, experiment, context);

        } catch (Exception e) {
            reportExperimentException(experiment.getKey(), e);
        }
        return null;
    }

    private boolean overrideShouldBeUsed(Experiment experiment) {
        String key = experiment.getKey();
        return userInfo().overridesExperiment(key) && experiment.containsValue(userInfo().getOverridenExperimentValue(key));
    }

    private <T> T valueFromOverride(TestResultConverter<T> resultConverter, String key) {
        return resultConverter.convert(userInfo().getOverridenExperimentValue(key));
    }

    private <T> T valueFromConduct(TestResultConverter<T> resultConverter, Experiment experiment, ConductionContext context) {
        Assignment assignment = experiment.conduct(context, userInfo());

        assignment.executeSideEffects(testGroupAssignmentTracker, userInfoStorage);

        generateExperimentReports(assignment, experiment);

        return assignment.result(resultConverter);
    }

    private void generateExperimentReports(Assignment assignment, Experiment experiment) {

        if (assignment.getExecutionTime() > maxConductionTimeMillis) {
            String message = String.format("Conducting of experiment %s took %s milliseconds while the expected time should be under %s milliseconds",
                    experiment.getId(), assignment.getExecutionTime(), maxConductionTimeMillis);
            laboratoryErrorHandler.handle(message, new SlowExperimentException(experiment), ExceptionType.SlowExperiment);
        }
        if (assignment.getTestGroup() != null)
            metricsReporter.reportConductExperiment(experiment.getId(), assignment.getTestGroup().getValue());

    }

    private ExistingTestGroups getExistingTestGroups(List<Experiment> experiments, ConductionContext context) {
        Option<UUID> uidToPersistBy = context.conductionStrategyOrFallback(userInfo()).persistentKernel();
        UUID uid = uidToPersistBy.isDefined() ? uidToPersistBy.get() : null;

        Map<String, String> testGroupsFromCookies = userInfo().getWinningExperiments(uid);

        Map<String, String>  testGroupsFromServer = ImmutableMap.of();
        if(serverStateIsRelevant(experiments, testGroupsFromCookies, uid)) {
            try {
                String cookieFromServer = petriClient.getUserState(uid);
                ExperimentsLog experimentLogFromServer = ExperimentsLog.parse(cookieFromServer).removeWhere(expired());
                appendServerStateToUserInfo(experimentLogFromServer, uid);
                testGroupsFromServer = experimentLogFromServer.getWinningTestGroups();
            } catch(Exception e) {
                laboratoryErrorHandler.handle(String.format("Unexpected exception while reading user state from server for user %s (user in session is %s) . falling back to cookies", uid, userInfo().getUserId()) , e, ExceptionType.ErrorReadingFromServer);
            }

        }
        return new ExistingTestGroups(testGroupsFromCookies, testGroupsFromServer);
    }

    private boolean serverStateIsRelevant(List<Experiment> experiments, Map<String, String> testGroupsFromCookies, UUID uid) {
        return petriTopology.isWriteStateToServer() &&
                uid != null &&
                atLeastOnePersistentKeyMissingInCookie(experiments, testGroupsFromCookies);
    }

    private void appendServerStateToUserInfo(ExperimentsLog experimentLogFromServer, UUID uid) {
        UserInfo updateUserInfo = userInfo().appendUserExperiments(experimentLogFromServer.serialized(), new Some(uid));
        if (!updateUserInfo.equals(userInfo()))
            userInfoStorage.write(updateUserInfo);
    }

    private boolean atLeastOnePersistentKeyMissingInCookie(List<Experiment> experiments, Map<String, String> testGroupsFromCookies) {
        Set<String> existingKeys = new HashSet<>();
        Set<String> suspectKeys = new HashSet<>();

        for (Experiment experiment : experiments) {
            if (experiment.shouldBePersisted()) {
                String key = experiment.getKey();
                if (testGroupsFromCookies.containsKey(valueOf(experiment.getId())))
                    existingKeys.add(key);
                else
                    suspectKeys.add(key);
            }
        }
        return !existingKeys.containsAll(suspectKeys);
    }

}
