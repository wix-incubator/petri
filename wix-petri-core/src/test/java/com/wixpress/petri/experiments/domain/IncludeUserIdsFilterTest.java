package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.userId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: uri
 * Date: 4/23/14
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncludeUserIdsFilterTest {

    private UUID uid;
    private IncludeUserIdsFilter filter;

    @Before
    public void setUp() throws Exception {
        uid = UUID.randomUUID();
        filter = new IncludeUserIdsFilter(uid);
    }

    @Test
    public void canBeSerializedAndDeserialized() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String serialized = objectMapper.writeValueAsString(filter);
        IncludeUserIdsFilter deserialized = objectMapper.readValue(serialized, IncludeUserIdsFilter.class);
        assertThat(deserialized, is(filter));
    }

    @Test
    public void eligibleForListedUsers() {
        final UserInfo userWithId = a(UserInfoMakers.UserInfo, with(userId, uid)).make();
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(userWithId)), is(true));
    }

    @Test
    public void isNonEligibleForNonListedUsers() {
        final UserInfo userWithId = a(UserInfoMakers.UserInfo, with(userId, UUID.randomUUID())).make();
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(userWithId)), is(false));
    }

    @Test
    public void treatsNullAsEmptyListOfIds() throws IOException {
        UUID[] emptyIds = new UUID[0];
        assertEquals(new IncludeUserIdsFilter((UUID[]) null), new IncludeUserIdsFilter(emptyIds));
    }

}
