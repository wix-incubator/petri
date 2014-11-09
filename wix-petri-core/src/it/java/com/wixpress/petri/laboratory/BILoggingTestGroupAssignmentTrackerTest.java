package com.wixpress.petri.laboratory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.wixpress.petri.LogDriver;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.petri.Clock;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.UserInfo;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.*;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;



/**
 * @author sagyr
 * @since 8/19/13
 */
public class BILoggingTestGroupAssignmentTrackerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private Clock clock = context.mock(Clock.class);
    private DateTime theTime = new DateTime();
    private BILoggingTestGroupAssignmentTracker biLogger = new BILoggingTestGroupAssignmentTracker(clock);
    private LogDriver logDriver = new LogDriver("target/bi-events-tests.log");


    @After
    public void clearLogbackConfiguration() throws Exception {
        logDriver.cleanup();
    }

    private Map<String, Object> biReportLine() throws IOException {
        assertThat(logDriver.logEntries(), is(not(empty())));
        String event = logDriver.logEntries().get(0);
        return ObjectMapperFactory.makeObjectMapper().readValue(event, Map.class);
    }

    private String[] parameterNamesOrdered() throws IOException {
        String event = logDriver.logEntries().get(0);
        Iterable<String> keyValuePairs = Splitter.on(',').split(strip(event, "{} "));
        Iterable<String> keys = transform(keyValuePairs, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(String input) {
                return strip(split(input, ':')[0], " \"");
            }
        });

        return Iterables.toArray(keys, String.class);
    }

    @Test
    public void generatesCorrectLogLineWithParametersOrder() throws Exception {
        UserInfo userInfo = a(UserInfo,
                with(ip, "THE_IP"),
                with(url, "THE_URL"),
                with(userAgent, "THE_UA"),
                with(language, "THE_LANGUAGE")).make();
        int testGroupId = 4;
        Experiment experiment = a(ExperimentMakers.Experiment).but(with(ExperimentMakers.id, 123)).make();

        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(returnValue(theTime));
        }});

        biLogger.newAssignment(userInfo, testGroupId, experiment);

        final String[] actual = parameterNamesOrdered();
        assertThat("log parameters order", actual, arrayContaining(
                "date",
                "cuid",
                "uuid",
                "label_id",
                "ip",
                "url",
                "user_agent",
                "lng",
                "experimentId",
                "testGroupId",
                "productName"));
    }

    @Test
    public void valuesFromAdditionsAddedToTheEndOfLogOverridingDefaultParameters() throws Exception {
        UserInfo userInfo = a(UserInfo,
                with(ip, "THE_IP"),
                with(url, "THE_URL"),
                with(userAgent, "THE_UA"),
                with(language, "THE_LANGUAGE")).make();
        int testGroupId = 4;
        Experiment experiment = a(ExperimentMakers.Experiment).but(with(ExperimentMakers.id, 123)).make();

        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(returnValue(theTime));
        }});

        biLogger.newAssignment(userInfo, testGroupId, new BIAdditions() {
            @Override
            public void contributeToBi(BIContributor contributor) {
                contributor
                        .put("string", "s")
                        .put("uuid", "overriden")
                        .put("int", 2)
                        .put("boolean", true);
            }
        }, experiment.getId(), experiment.getScope());

        assertThat(biReportLine(), allOf(
                hasEntry("date", (Object) theTime.toString(ISODateTimeFormat.dateTime())),
                hasEntry("cuid", (Object) userInfo.clientId.toString()),
                hasEntry("uuid", (Object) "overriden"),
                hasEntry("label_id", (Object) (experiment.getId() * 10000 + testGroupId)),
                hasEntry("ip", (Object) userInfo.ip),
                hasEntry("url", (Object) userInfo.url),
                hasEntry("user_agent", (Object) userInfo.userAgent),
                hasEntry("lng", (Object) userInfo.language),
                hasEntry("experimentId", (Object) experiment.getId()),
                hasEntry("testGroupId", (Object) testGroupId),
                hasEntry("string", (Object) "s"),
                hasEntry("int", (Object) 2),
                hasEntry("boolean", (Object) true)));

    }

    @Test
    public void generatesCorrectLogLine() throws Exception {
        UserInfo userInfo = a(UserInfo,
                with(ip, "THE_IP"),
                with(url, "THE_URL"),
                with(userAgent, "THE_UA"),
                with(language, "THE_LANGUAGE")).make();
        int testGroupId = 4;
        Experiment experiment = a(ExperimentMakers.Experiment).but(with(ExperimentMakers.id, 123)).make();

        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(returnValue(theTime));
        }});

        biLogger.newAssignment(userInfo, testGroupId, experiment);

        assertThat(biReportLine(), allOf(
                hasEntry("date", (Object) theTime.toString(ISODateTimeFormat.dateTime())),
                hasEntry("cuid", (Object) userInfo.clientId.toString()),
                hasEntry("uuid", (Object) userInfo.getUserId().toString()),
                hasEntry("label_id", (Object) (experiment.getId() * 10000 + testGroupId)),
                hasEntry("ip", (Object) userInfo.ip),
                hasEntry("url", (Object) userInfo.url),
                hasEntry("user_agent", (Object) userInfo.userAgent),
                hasEntry("lng", (Object) userInfo.language),
                hasEntry("experimentId", (Object) experiment.getId()),
                hasEntry("testGroupId", (Object) testGroupId),
                hasEntry("productName", (Object) experiment.getScope())));
    }

}
