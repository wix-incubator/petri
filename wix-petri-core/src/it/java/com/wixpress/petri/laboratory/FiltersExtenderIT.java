package com.wixpress.petri.laboratory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import filters.AdditionalFilter;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.filters;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FiltersExtenderIT {

    @Test
    public void experimentsCanBeSerializedWithExtendedFilterTypesFromClassPath() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        AdditionalFilter additionalFilterFromClasspath = new AdditionalFilter();
        Experiment experimentWithNewFilterType = an(ExperimentMakers.Experiment, with(filters,
                asList(new FirstTimeVisitorsOnlyFilter(), additionalFilterFromClasspath)
        )).make();
        String json = objectMapper.writeValueAsString(experimentWithNewFilterType);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(experimentWithNewFilterType));
    }

    @Test
    public void filterTypesCanBeExtendedWithDynamicallyLoadedClassesFromMatchingJars() throws IOException {
        //relevant jar is committed as 'sample-extended-filters.jar' under petri-plugins folder
        //(once api module is extracted this jar can be created before petri-core and copied there, same as in the e2e-tests)

        FilterTypeIdResolver.useDynamicFilterClassLoading("dynamic.filters");

        Map<String, Class<? extends Filter>> extendedTypes = ExtendedFilterTypesIds.extendedTypes();
        assertTrue("SomeCustomFilter was not dynamically loaded", extendedTypes.containsKey("SomeCustomFilter"));
        assertThat(extendedTypes.get("SomeCustomFilter").getName(), is("dynamic.filters.SomeCustomFilter"));
    }

}