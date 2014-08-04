package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.*;

public class Assignment {
    private final BIAdditions biAdditions;
    private final UserInfo userInfo;
    private final TestGroup testGroup;
    private final boolean isToggle;
    private final int experimentId;
    private final String experimentScope;
    private final boolean isOnlyForLoggedInUsers;
    private final boolean isPersistent;

    //TODO - extract the field copying of the experiment to a builder
    public Assignment(BIAdditions biAdditions, UserInfo userInfo, TestGroup testGroup, Experiment experiment) {
        this.biAdditions = biAdditions;
        this.userInfo = userInfo;
        this.testGroup = testGroup;
        this.isToggle = experiment.isToggle();
        this.experimentId = experiment.getId();
        this.experimentScope = experiment.getScope();
        this.isOnlyForLoggedInUsers = experiment.isOnlyForLoggedInUsers();
        this.isPersistent = experiment.isPersistent();
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

    public <T> T result(TestResultConverter<T> resultConverter) {
        if (testGroup == null)
            return null;
        return resultConverter.convert(testGroup.getValue());
    }

    //TODO - extract to callbacks on the trackableLaboratory?
    public void executeSideEffects(TestGroupAssignmentTracker testGroupAssignmentTracker, UserInfoStorage userInfoStorage) {
        if (!isToggle && testGroup != null) {

            testGroupAssignmentTracker.newAssignment(this);

            if (isPersistent) {
                String logToAppend = ExperimentsLog.empty().appendExperiment(experimentId, testGroup.getId()).serialized();

                if (isOnlyForLoggedInUsers)
                    userInfoStorage.write(userInfo.appendUserExperiments(logToAppend));
                else
                    userInfoStorage.write(userInfo.appendAnonymousExperiments(logToAppend));
            }
        }
    }


}
