package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * To change this template use File | Settings | File Templates.
 */
public class UiFilterTemplateTest {

    @Test
    public void canBeSerialized() throws IOException {
        UiFilter uiFilterTemplate = (new UiFilterBuilder()
                .withFilterName("geo")
                .withEnabled(true)
                .withMandatoryValue(asList("Israel"))
                .withOptionalValue(asList("En","UK")))
                .build();
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();

        String json = objectMapper.writeValueAsString(uiFilterTemplate);
        UiFilter serialized = objectMapper.readValue(json, new TypeReference<UiFilter>() {
        });
        assertEquals(serialized, uiFilterTemplate);
    }

}
