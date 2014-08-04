package com.wixpress.petri.petri;

import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sagyr
 * @since 10/3/13
 */
public class SpecsSynchronizer {

    private final PetriClient pc;
    private final SpecDefinitions specsDefinitions;
    private final Clock clock;

    public SpecsSynchronizer(PetriClient petriClient, SpecDefinitions specDefinitions, Clock clock) {
        this.pc = petriClient;
        this.specsDefinitions = specDefinitions;
        this.clock = clock;
    }

    public void syncSpecs() {
        DateTime now = clock.getCurrentDateTime();

        List<ExperimentSpec> specs = new ArrayList<>();
        List<SpecDefinition> specDefinitions = specsDefinitions.get();

        for (SpecDefinition specDefinition : specDefinitions) {
            specs.add(specDefinition.create(now));
        }
        pc.addSpecs(specs);
    }

}
