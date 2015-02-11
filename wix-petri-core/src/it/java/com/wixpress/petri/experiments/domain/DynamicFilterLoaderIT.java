package com.wixpress.petri.experiments.domain;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DynamicFilterLoaderIT {

    //relevant jar is committed as 'sample-extended-filters.jar' under petri-plugins folder
    //(once api module is extracted this jar can be created before petri-core and copied there, same as in the e2e-tests)


    @Test
    public void filterTypeCanBeDynamicallyLoadedFromMatchingJar() throws IOException {
        DynamicFilterLoader dynamicFilterLoader = new DynamicFilterLoader("dynamic.filters");
        List<Class> filterTypesFromJars = dynamicFilterLoader.loadFilterTypesFromJars();
        assertThat(filterTypesFromJars.size(), is(1));
        assertThat(filterTypesFromJars.get(0).getName(), is("dynamic.filters.SomeCustomFilter"));
    }
}