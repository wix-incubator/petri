package com.wixpress.petri.petri;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;

import java.util.List;
import java.util.UUID;

/**
 * @author talyag
 * @since 9/24/14
 */
//TODO - move this class to petri-server module
public interface FullPetriClient {

    List<Experiment> fetchAllExperiments();

    List<Experiment> fetchAllExperimentsGroupedByOriginalId();

    Experiment insertExperiment(ExperimentSnapshot snapshot);

    Experiment updateExperiment(Experiment experiment);

    List<ExperimentSpec> fetchSpecs();

    void addSpecs(List<ExperimentSpec> expectedSpecs);

    List<Experiment> getHistoryById(int id);

    void deleteSpec(String key);

    List<ConductExperimentSummary> getExperimentReport(int experimentId);

    public class PetriException extends RuntimeException {
        public PetriException(Throwable e) {
            super(e);
        }

        public PetriException(String msg) {
            super(msg);
        }
    }

    public class CreateFailedData {
        public String clazz;
        public String key;

        public CreateFailedData() {
        }

        public CreateFailedData(String clazz, String key) {
            this.clazz = clazz;
            this.key = key;
        }
    }

    public class CreateFailed extends PetriException {

        public CreateFailedData data;

        public CreateFailed(@JsonProperty("data") CreateFailedData createFailedData) {
            super(String.format("unable to add a %s with key '%s'", createFailedData.clazz, createFailedData.key));
            this.data = createFailedData;
        }
    }

    public class UpdateFailed extends PetriException {

        public Experiment data;

        @JsonCreator
        public UpdateFailed(@JsonProperty("data") Experiment experiment) {
            super(String.format("Failed to update experiment %s. This can be due to either \n" +
                    " 1 - stale edit\n" +
                    " 2 - spec with matching key does not exist anymore\n" +
                    " 3 - experiment with such id does not exist\n" +
                    " Experiment - '%s'", experiment.getId(), experiment));
            this.data = experiment;

        }
    }

}
