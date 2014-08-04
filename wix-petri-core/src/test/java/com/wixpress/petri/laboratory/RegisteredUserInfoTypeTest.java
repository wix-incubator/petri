package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 12/24/13
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisteredUserInfoTypeTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TestGroupSelector selector = context.mock(TestGroupSelector.class);

    @Test
    public void drawsTestGroupByUserId() throws Exception {
        final UUID uid = UUID.randomUUID();
        final Experiment someExperiment = an(Experiment).make();
        final TestGroup someTestGroup = new TestGroup();
        RegisteredUserInfoType type = new RegisteredUserInfoType(selector, uid);
        context.checking(new Expectations(){{
            oneOf(selector).forWixUser(someExperiment, uid); will(returnValue(someTestGroup));
        }});
        assertThat(type.drawTestGroup(someExperiment), is(someTestGroup));
    }
}
