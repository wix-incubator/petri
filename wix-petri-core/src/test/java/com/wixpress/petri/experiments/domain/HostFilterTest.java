package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.host;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: itayk
 * Date: 22/07/14
 */
public class HostFilterTest {

    @Test
    public void testIsEligiblePositive() throws Exception {
        String myHost = "foo";
        UserInfo userWithMyHost = a(UserInfoMakers.UserInfo, with(host, myHost)).make();

        HostFilter hostFilter = new HostFilter(asList(myHost));
        assertThat(hostFilter.isEligible(userWithMyHost, null), is(true));
    }

    @Test
    public void testIsEligibleNegative() throws Exception {
        String myHost = "foo";
        String differentHost = "bar";
        UserInfo userWithDifferentHost = a(UserInfoMakers.UserInfo, with(host, differentHost)).make();

        HostFilter hostFilter = new HostFilter(asList(myHost));
        assertThat(hostFilter.isEligible(userWithDifferentHost, null), is(false));
    }

}
