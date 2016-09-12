package com.wixpress.guineapig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.wixpress.guineapig.drivers.JsonResponse;
import com.wixpress.guineapig.entities.ui.UiSpec;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.wixpress.guineapig.TestUtils.extractCollectionPayload;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: igalharel
 * Date: 9/23/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class SpecsIT extends BrokenITBase {

    private void givenPetriContainsSpec(String key) {
        ramPetriClient.addSpecs(ImmutableList.of(aNewlyGeneratedExperimentSpec(key).withOwner("tttt").build()));
    }

    private List<UiSpec> getSpecs() throws IOException {
        JsonResponse response = httpDriver.get(BASE_IP + "Specs");
        return extractCollectionPayload(response, new TypeReference<List<UiSpec>>() {
        });
    }


    @Test
    public void getSpecsForDisplay() throws IOException {
        givenPetriContainsSpec("aSpec");

        List<UiSpec> deSerialized = getSpecs();
        assertThat(deSerialized.get(0).getKey(), is("aSpec"));
    }

    @Test
    public void deleteSpec() throws IOException {

        String specKey = "aSpec.a";

        givenPetriContainsSpec(specKey);


        JsonResponse response = httpDriver.post(BASE_IP + "deleteSpecs/" + specKey);

        assertTrue(response.getSuccess());

        getSpecs();
        assertThat(getSpecs().size(), is(0));
    }

}
