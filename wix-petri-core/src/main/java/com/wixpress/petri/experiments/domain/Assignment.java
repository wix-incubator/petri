package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.*;

public class Assignment {
    private final BIAdditions biAdditions;
    private final UserInfo userInfo;
    private final ConductionStrategy conductionStrategy;
    private final TestGroup testGroup;
    private final boolean isToggle;
    private final int experimentId;
    private final String experimentScope;
    private final boolean isOnlyForLoggedInUsers;
    private final long executionTime;

    //TODO - extract the field copying of the experiment to a builder
    public Assignment(UserInfo userInfo, ConductionStrategy conductionStrategy, BIAdditions biAdditions, TestGroup testGroup, Experiment experiment, long executionTime) {
        this.biAdditions = biAdditions;
        this.userInfo = userInfo;
        this.conductionStrategy = conductionStrategy;
        this.testGroup = testGroup;
        this.executionTime = executionTime;
        this.isToggle = experiment.isToggle();
        this.experimentId = experiment.getId();
        this.experimentScope = experiment.getScope();
        this.isOnlyForLoggedInUsers = experiment.isOnlyForLoggedInUsers();
    }

    public BIAdditions getBiAdditions() {
        return biAdditions;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public int getExperimentId() {
        return experimentId;
    }

    public String getScope() {
        return experimentScope;
    }

    public TestGroup getTestGroup() {
        return testGroup;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public <T> T result(TestResultConverter<T> resultConverter) {
        if (testGroup == null)
            return null;
        return resultConverter.convert(testGroup.getValue());
    }

    public void executeSideEffects(TestGroupAssignmentTracker testGroupAssignmentTracker, UserInfoStorage userInfoStorage) {

        if (!isToggle && testGroup != null) {

            testGroupAssignmentTracker.newAssignment(this);

            String logToAppend = ExperimentsLog.empty().appendExperiment(experimentId, testGroup.getId()).serialized();

            if (conductionStrategy.shouldPersist()) {
                if (isOnlyForLoggedInUsers) {
                    userInfoStorage.write(userInfo.appendUserExperiments(logToAppend, conductionStrategy.persistentKernel()));
                } else
                    userInfoStorage.write(userInfo.appendAnonymousExperiments(logToAppend));
            }
        }
    }


}
