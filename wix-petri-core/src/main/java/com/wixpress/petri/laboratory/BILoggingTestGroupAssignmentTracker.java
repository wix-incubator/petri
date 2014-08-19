package com.wixpress.petri.laboratory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wixpress.petri.experiments.domain.Assignment;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.petri.Clock;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BILoggingTestGroupAssignmentTracker implements TestGroupAssignmentTracker {
    private static final String BI_LOGGER_NAME = "experimentsLog";

    private final Logger biLog = LoggerFactory.getLogger(BI_LOGGER_NAME);
    private final Clock clock;

    public BILoggingTestGroupAssignmentTracker(Clock clock) {
        this.clock = clock;
    }

    //TODO - delete
    public void newAssignment(UserInfo userInfo, int testGroupId, Experiment experiment) {
        newAssignment(userInfo, testGroupId, BIAdditions$.MODULE$.Empty(), experiment.getId(), experiment.getScope());
    }

    public void newAssignment(UserInfo userInfo, int testGroupId, BIAdditions additions, int experimentId, String experimentScope) {
        UUID clientId = userInfo.clientId;


        ObjectNode node = JsonNodeFactory.instance.objectNode();

        node
                .put("date", clock.getCurrentDateTime().toString(ISODateTimeFormat.dateTime()))
                .put("cuid", clientId != null ? clientId.toString() : null)
                .put("uuid", userInfo.getUserId() != null ? userInfo.getUserId().toString() : null)
                .put("label_id", experimentId * 10000 + testGroupId)
                .put("ip", userInfo.ip)
                .put("url", userInfo.url)
                .put("user_agent", userInfo.userAgent)
                .put("lng", userInfo.language)
                .put("experimentId", experimentId)
                .put("testGroupId", testGroupId)
                .put("productName", experimentScope);

        additions.contributeToBi(BIContributor$.MODULE$.forJackson(node));

        biLog.info(node.toString());
    }

    @Override
    public void newAssignment(Assignment assignment) {
        newAssignment(assignment.getUserInfo(), assignment.getTestGroup().getId(),
                assignment.getBiAdditions(),
                assignment.getExperimentId(), assignment.getScope());
    }
}
