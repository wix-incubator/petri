package com.wixpress.guineapig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.guineapig.drivers.JsonResponse;
import com.wixpress.guineapig.dsl.ExperimentBuilders;
import com.wixpress.guineapig.dsl.UiExperimentMakers;
import com.wixpress.guineapig.entities.ui.ExperimentReport;
import com.wixpress.guineapig.entities.ui.*;
import com.wixpress.guineapig.services.NoOpFilterAdapterExtender;
import com.wixpress.guineapig.util.GuineaPigDBDriver;
import com.wixpress.guineapig.util.ReportMatchers;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.WixEmployeesFilter;
import com.wixpress.petri.petri.ConductExperimentReport;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.guineapig.entities.ui.UiExperimentBuilder.aCopyOf;
import static com.wixpress.guineapig.services.MetaDataConsts.PUBLIC_URL;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: igalharel
 * Date: 9/23/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class PetriWebappIT extends BrokenITBase {

    private final ExperimentConverter converter = new ExperimentConverter(new AlwaysTrueIsEditablePredicate(), new NoOpFilterAdapterExtender());
    @Resource
    GuineaPigDBDriver guineaPigDBDriver;

    @Before
    public void init() {
        guineaPigDBDriver.reloadSchema();
    }

    private <T> T extractPayload(JsonResponse response, TypeReference<T> typeReference) throws IOException {
        return om.readValue(response.getPayload(), typeReference);
    }

    private ExperimentReport getExperimentReport(int experimentId) throws IOException {
        JsonResponse response = httpDriver.get(BASE_API_URL + "Experiments/report/" + experimentId);
        return extractPayload(response, new TypeReference<ExperimentReport>() {
        });
    }

    private UiExperiment getExperiment(int experimentId) throws IOException {
        JsonResponse response2 = httpDriver.get(BASE_API_URL + "Experiment/" + experimentId);
        return extractPayload(response2, new TypeReference<UiExperiment>() {
        });
    }

    private boolean update(UiExperiment uiExperiment) {
        final JsonResponse response = httpDriver.put(BASE_API_URL + "Experiment/THIS_SEEMS_UNNECESSARY", uiExperiment);
        JSONObject responseJson = response.getBodyJson();
        return responseJson.getBoolean("success");
    }

    private boolean add(UiExperiment uiExperiment) {
        final JsonResponse response = httpDriver.post(BASE_API_URL + "Experiments", uiExperiment);
        JSONObject responseJson = response.getBodyJson();
        return responseJson.getBoolean("success");
    }

    private boolean restExperimentCommand(int id, String command) {
        final JsonResponse response = httpDriver.post(BASE_API_URL + "Experiment/" + id + "/" + command, id);
        JSONObject responseJson = response.getBodyJson();
        return responseJson.getBoolean("success");
    }


    private JSONObject restExperimentCommandReturnPayload(int id, String command) {
        return httpDriver.post(BASE_API_URL + "Experiment/" + id + "/" + command, id).getBodyJson();
    }


    public String getSpecExposurIdAsString() {
        JsonResponse response = httpDriver.get(BASE_API_URL + "specExposures");
        JSONObject responseJson = response.getBodyJson();
        return responseJson.get("payload").toString();
    }

    @Test
    public void getExperiment() throws IOException, ClassNotFoundException {
        givenPetriContains(an(Experiment,
                with(id, 1),
                with(key, "aKey")));

        List<UiExperiment> deSerialized = getExperiments();
        assertThat(deSerialized.get(0).getKey(), is("aKey"));
    }

    @Test
    public void canCreateAndUpdateExperimentWithoutSpec() throws IOException, ClassNotFoundException {

        final Maker<Experiment> experiment = ExperimentBuilders.createActive().but(
                with(id, 1),
                with(key, "anyKey"),
                with(fromSpec, false));

        givenPetriContains(experiment.make());

        UiExperiment newExperiment = getExperiment(1);
        add(newExperiment);
        assertThat(newExperiment.getKey(), is("anyKey"));

        update(converter.convert(experiment.but(
                with(description, "DESCRIPTION")).make()));
        UiExperiment updatedExperiment = getExperiment(1);
        assertThat(updatedExperiment.getDescription(), is("DESCRIPTION"));
    }

    @Test
    public void updateExperiment() throws IOException, ClassNotFoundException {

        final Maker<Experiment> experiment = ExperimentBuilders.createActive().but(
                with(id, 1),
                with(description, "THE CURRENT DESCRIPTION"));
        givenPetriContains(experiment.make());

        final UiExperiment updatedUIExperiment = converter.convert(experiment.but(
                with(description, "THE NEW DESCRIPTION")).make());

        boolean status = update(updatedUIExperiment);
        assertTrue(status);
        assertThat(getExperiment(updatedUIExperiment.getId()).getDescription(), is("THE NEW DESCRIPTION"));
    }


    @Test
    public void terminateExperiment() throws IOException {
        Experiment activeExperiment = ExperimentBuilders.createActive().but(
                with(id, 1))
                .make();

        givenPetriContains(activeExperiment);

        JSONObject response = restExperimentCommandReturnPayload(activeExperiment.getId(), "terminate");
        assertThat(response.getJSONObject("payload").getBoolean("specCanBeDeleted"), is(true));
        UiExperiment terminated = getExperiment(activeExperiment.getId());
        assertThat(terminated.getState(), is(ExperimentState.ENDED.getState()));
    }

    @Test
    public void terminateExperimentWithNoSpecDoesntResultInSuggestionToDelete() {
        final Experiment activeExperiment = ExperimentBuilders.createFuture().but(
                with(id, 1),
                with(fromSpec, false))
                .make();
        givenPetriContains(activeExperiment);

        JSONObject response = restExperimentCommandReturnPayload(activeExperiment.getId(), "terminate");
        assertThat(response.getJSONObject("payload").getBoolean("specCanBeDeleted"), is(false));
        assertThat(response.getJSONObject("payload").getString("specKey"), is(activeExperiment.getKey()));
    }

    @Test
    public void pauseExperiment() throws IOException {
        Experiment activeExperiment = ExperimentBuilders.createActive().but(
                with(id, 1),
                with(paused, false))
                .make();

        givenPetriContains(activeExperiment);
        boolean status = restExperimentCommand(activeExperiment.getId(), "pause");
        assertTrue(status);
        UiExperiment paused = getExperiment(activeExperiment.getId());
        assertThat(paused.getState(), is(ExperimentState.PAUSED.getState()));
    }

    @Test
    public void resumeExperiment() throws IOException {
        Experiment activeExperiment = ExperimentBuilders.createActive().but(
                with(id, 1),
                with(paused, true))
                .make();

        givenPetriContains(activeExperiment);
        boolean status = restExperimentCommand(activeExperiment.getId(), "resume");
        assertTrue(status);
        UiExperiment paused = getExperiment(activeExperiment.getId());
        assertThat(paused.getState(), is(ExperimentState.ACTIVE.getState()));
    }

    @Test
    public void expandExperiment() throws IOException, ClassNotFoundException {

        final Maker<Experiment> experiment = ExperimentBuilders.createActive().but(
                with(id, 1));
        givenPetriContains(experiment.make());

        final UiExperiment updatedUIExperimentWithChangeInFilters = converter.convert(experiment.but(with(filters, ImmutableList.of(new WixEmployeesFilter()))).make());
        update(updatedUIExperimentWithChangeInFilters);

        List<UiExperiment> uiExperiments = getExperiments();
        assertThat(uiExperiments.size(), is(1));
        assertThat(uiExperiments.get(0).getId(), is(2));
    }

    @Test
    public void getExperimentReports() throws IOException {
        int experimentId = 1;
        ConductExperimentReport conductExperimentReport = new ConductExperimentReport("localhost", experimentId, "value", 1L);
        ramPetriClient.reportConductExperiment(ImmutableList.of(conductExperimentReport));

        final ExperimentReport experimentReport = getExperimentReport(experimentId);
        assertThat(experimentReport, is(ReportMatchers.hasOneCountForValue("value")));
    }

    @Test
    public void canLimitExperimentConduction() throws IOException {
        UiExperiment uiExperiment = make(a(UiExperimentMakers.UiExperiment,
                with(UiExperimentMakers.conductionLimit, 1),
                with(UiExperimentMakers.specKey, false),
                with(UiExperimentMakers.scope, PUBLIC_URL)));

        add(uiExperiment);

        UiExperiment uiExperimentFromServer = getExperiment(1);
        assertEquals(uiExperimentFromServer.getConductLimit(), 1);

        UiExperiment updatedUiExperiment = aCopyOf(uiExperimentFromServer).withConductLimit(100).build();
        update(updatedUiExperiment);

        UiExperiment updatedUiExperimentFromServer = getExperiment(1);
        assertEquals(updatedUiExperimentFromServer.getConductLimit(), 100);
    }

}
