package com.wixpress.petri.laboratory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.FirstTimeVisitorsOnlyFilter;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import filters.AdditionalFilter;
import org.junit.Test;

import java.io.IOException;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.filters;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FiltersExtenderIT {

    @Test
    public void experimentsCanBeSerializedWithExtendedFilterTypesFromClassPath() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        Experiment experimentWithNewFilterType = an(ExperimentMakers.Experiment, with(filters,
                asList(new FirstTimeVisitorsOnlyFilter(), new AdditionalFilter())
        )).make();
        String json = objectMapper.writeValueAsString(experimentWithNewFilterType);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(experimentWithNewFilterType));
    }

}