package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 12/24/13
 * Time: 4:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousUserInfoTypeTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TestGroupSelector selector = context.mock(TestGroupSelector.class);

    @Test
    public void testDrawTestGroup() throws Exception {
        AnonymousUserInfoType type = new AnonymousUserInfoType(selector);
        final Experiment someExperiment = an(Experiment).make();
        final TestGroup someGroup = new TestGroup();
        context.checking(new Expectations(){{
            oneOf(selector).forAnonymousUsers(someExperiment);
            will(returnValue(someGroup));
        }});
        assertThat(type.drawTestGroup(someExperiment), is(someGroup));
    }
}
