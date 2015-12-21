package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.junit.Test;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RegisteredUserInfoTypeTest{

    @Test
    public void drawsTestGroupByUserId() throws Exception {
        final UUID uid = UUID.fromString("73699180-bcf1-4bf3-8d04-676b8444b691");
        final Experiment someExperiment = an(ExperimentMakers.Experiment)
                .but(with(ExperimentMakers.id,3)).make();
        RegisteredUserInfoType type = new RegisteredUserInfoType(uid);
        assertThat(type.drawTestGroup(someExperiment).getId(), is(1));
    }
}