package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.host;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: itayk
 * Date: 22/07/14
 */
public class ArtifactFilterTest {

    @Test
    public void testIsEligiblePositive() throws Exception {
        String myArtifact = "foo.wixpress.com";
        UserInfo userWithMyArtifact = a(UserInfoMakers.UserInfo, with(host, myArtifact)).make();

        ArtifactFilter artifactFilter = new ArtifactFilter(asList(myArtifact));
        assertThat(artifactFilter.isEligible(defaultEligibilityCriteriaForUser(userWithMyArtifact)), is(true));
    }

    @Test
    public void testIsEligibleNegative() throws Exception {
        String myArtifact = "foo.wixpress.com";
        String differentArtifact = "bar.wixpress.com";
        UserInfo userWithDifferentHost = a(UserInfoMakers.UserInfo, with(host, differentArtifact)).make();

        ArtifactFilter artifactFilter = new ArtifactFilter(asList(myArtifact));
        assertThat(artifactFilter.isEligible(defaultEligibilityCriteriaForUser(userWithDifferentHost)), is(false));
    }

}
