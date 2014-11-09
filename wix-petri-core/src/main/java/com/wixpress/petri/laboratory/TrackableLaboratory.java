package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Assignment;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.petri.SpecDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sagyr
 * @since 8/7/13
 */
public class TrackableLaboratory implements Laboratory, PublicLaboratory {

    private final int maxConductionTimeMillis;
    private final Experiments experiments;
    private final UserInfoStorage userInfoStorage;
    private final TestGroupAssignmentTracker testGroupAssignmentTracker;
    private final ErrorHandler laboratoryErrorHandler;

    public TrackableLaboratory(Experiments experiments, TestGroupAssignmentTracker testGroupAssignmentTracker, UserInfoStorage userInfoStorage, ErrorHandler laboratoryErrorHandler, int maxConductionTimeMillis) {
        this.experiments = experiments;
        this.testGroupAssignmentTracker = testGroupAssignmentTracker;
        this.userInfoStorage = userInfoStorage;
        this.laboratoryErrorHandler = laboratoryErrorHandler;
        this.maxConductionTimeMillis = maxConductionTimeMillis;
    }

    private UserInfo userInfo() {
        return userInfoStorage.read();
    }

    private void reportExperimentException(String experimentKey, Throwable cause) {
        reportException("Unexpected exception while conducting experiment with key - '" + experimentKey + "', due to : " + cause.getMessage(), cause);
    }

    private void reportException(String message, Throwable cause) {
        laboratoryErrorHandler.handle(message, cause);
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

            for (Experiment experiment : experimentsByKey) {
                T result = calcExperimentValue(resultConverter, experiment, context);
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
            reportException("Unexpected exception while conducting all experiments for scope - " + scope, e);
            return new HashMap<>();
        }
    }


    private Map<String, String> conductAll(List<Experiment> experiments, ConductionContext context) {
        StringConverter stringResultConverter = new StringConverter();
        HashMap<String, String> results = new HashMap<>();

        for (Experiment experiment : experiments) {
            String key = experiment.getKey();
            if (!results.containsKey(key)) {
                String result = calcExperimentValue(stringResultConverter, experiment, context);
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

    private <T> T calcExperimentValue(TestResultConverter<T> resultConverter, Experiment experiment, ConductionContext context) {
        if (userInfo().isRobot) {
            return null;
        }
        try {
            if (overrideShouldBeUsed(experiment)) {
                return valueFromOverride(resultConverter, experiment.getKey());
            } else {
                TestGroup existingTestGroup = previousValueFromLog(experiment);
                if (!experiment.isToggle() && existingTestGroup != null) {
                    userInfoStorage.write(userInfo());
                    return resultConverter.convert(existingTestGroup.getValue());
                } else
                    return valueFromConduct(resultConverter, experiment, context);
            }
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

    private TestGroup previousValueFromLog(Experiment experiment) {
        if (userInfo().participatesInExperiment(experiment.getId())) {
            final int testGroupId = userInfo().winningTestGroupID(experiment.getId());
            return experiment.getTestGroupById(testGroupId);
        }
        return null;
    }

    private <T> T valueFromConduct(TestResultConverter<T> resultConverter, Experiment experiment, ConductionContext context) {
        Assignment assignment = conductExperimentAndNotifySlowCalculationTime(context, experiment);

        assignment.executeSideEffects(testGroupAssignmentTracker, userInfoStorage);

        return assignment.result(resultConverter);
    }

    private Assignment conductExperimentAndNotifySlowCalculationTime(ConductionContext context, Experiment experiment) {
        long startTime = System.currentTimeMillis();
        Assignment result = experiment.conduct(context, userInfo());
        long endTime = System.currentTimeMillis();
        double totalTime = endTime - startTime;

        if (totalTime > maxConductionTimeMillis) {
            String message = String.format("Conducting of experiment %s took %s milliseconds while the expected time should be under %s milliseconds",
                    experiment.getId(), totalTime, maxConductionTimeMillis);
            reportException(message, new SlowExperimentException(experiment));
        }
        return result;
    }


}
