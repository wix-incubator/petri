package com.wixpress.petri.petri;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/10/13
 */
public interface PetriClient {

    //TODO - should be split into different clients or more importantly data types
    //(Experiment for laboratory can be much thinner than for GP)

    List<Experiment> fetchActiveExperiments();

    List<Experiment> fetchAllExperiments();

    List<Experiment> fetchAllExperimentsGroupedByOriginalId();

    Experiment insertExperiment(ExperimentSnapshot snapshot);

    Experiment updateExperiment(Experiment experiment);

    List<ExperimentSpec> fetchSpecs();

    void addSpecs(List<ExperimentSpec> expectedSpecs);


    List<Experiment> getHistoryById(int id);

    void deleteSpec(String key);


    public class PetriException extends RuntimeException {
        public PetriException(Throwable e) {
            super(e);
        }

        public PetriException(String msg) {
            super(msg);
        }
    }

    public class CreateFailed extends PetriException {
        public CreateFailed(@JsonProperty("aClass") Class<?> aClass, @JsonProperty("key") String key) {
            super(String.format("unable to add a %s with key '%s'", aClass, key));
        }
    }

    //TODO - should only receive 1 parameter - experiment, and use its getId() method
    // but that causes the JsonRpcProtocolClient to fail on the
    // e = getReader().readValue(getReader().treeAsTokens(errorObject.get("data")),Exception.class);
    public class UpdateFailed extends PetriException {
        public UpdateFailed(@JsonProperty("experiment") Experiment experiment, @JsonProperty("id") int id) {
            super(String.format("Failed to update experiment %s. This can be due to either \n" +
                    " 1 - stale edit\n" +
                    " 2 - spec with matching key does not exist anymore\n" +
                    " 3 - experiment with such id does not exist\n" +
                    " Experiment - '%s'", id, experiment));
        }
    }
}
