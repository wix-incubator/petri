package com.wixpress.petri.laboratory;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExperimentOverridesUrlDecoderTest{

    ExperimentOverridesUrlDecoder experimentOverridesUrlDecoder = new ExperimentOverridesUrlDecoder();

    @Test
    public void getSingleExperimentOverridesFromUrlParam(){
        String experimentOverridesUrlParam = "some-key:some-value";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>(){{put("some-key", "some-value");}};

        Map<String,String> experimentOverridesResult = experimentOverridesUrlDecoder.decode(experimentOverridesUrlParam);

        assertThat (experimentOverridesResult, is(expectedExperimentOverrides));
    }

    @Test
    public void getMultipleExperimentOverridesFromUrlParam(){
        String experimentOverridesUrlParam = "some-key:some-value;other-key:other-value";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>(){{
            put("some-key", "some-value");
            put("other-key", "other-value");}};

        Map<String,String> experimentOverridesResult = experimentOverridesUrlDecoder.decode(experimentOverridesUrlParam);

        assertThat (experimentOverridesResult, is(expectedExperimentOverrides));
    }

    @Test
    public void getDefaultExperimentOverridesFromUrlParam(){
        String experimentOverridesUrlParam = "";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>();

        Map<String,String> experimentOverridesResult = experimentOverridesUrlDecoder.decode(experimentOverridesUrlParam);

        assertThat (experimentOverridesResult, is(expectedExperimentOverrides));
    }

    @Test
    public void getDefaultExperimentOverridesWhenNullUrlParam(){
        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>();

        Map<String,String> experimentOverridesResult = experimentOverridesUrlDecoder.decode(null);

        assertThat (experimentOverridesResult, is(expectedExperimentOverrides));
    }

    @Test
    public void getMultipleExperimentOverridesFromUrlParamWithCorruptedExperimentsNoValue(){
        String corruptedExperimentOverridesUrlParam = "some-key:some-value;other-key:other-value;notgood";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>(){{
            put("some-key", "some-value");
            put("other-key", "other-value");}};

        Map<String,String> corruptedExperimentOverridesResult = experimentOverridesUrlDecoder.decode(corruptedExperimentOverridesUrlParam);

        assertThat (corruptedExperimentOverridesResult, is(expectedExperimentOverrides));

    }

    @Test
    public void getDefaultExperimentOverridesFromUrlParamWithCorruptedExperimentsNoDelimiter() {
        String corruptedExperimentOverridesUrlParam = "this:is:not:valid";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>();

        Map<String,String> anotherCorruptedExperimentOverridesResult = experimentOverridesUrlDecoder.decode(corruptedExperimentOverridesUrlParam);

        assertThat (anotherCorruptedExperimentOverridesResult, is(expectedExperimentOverrides));
    }

    @Test
    public void getDefaultExperimentOverridesFromUrlParamWithCorruptedUrl() {
        String corruptedExperimentOverridesUrlParam = "this is a corrupted url";

        Map<String,String> expectedExperimentOverrides = new HashMap<String, String>();

        Map<String,String> anotherCorruptedExperimentOverridesResult = experimentOverridesUrlDecoder.decode(corruptedExperimentOverridesUrlParam);

        assertThat (anotherCorruptedExperimentOverridesResult, is(expectedExperimentOverrides));
    }
}