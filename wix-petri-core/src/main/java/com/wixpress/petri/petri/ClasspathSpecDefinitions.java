package com.wixpress.petri.petri;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author sagyr
 * @since 10/3/13
 */
public class ClasspathSpecDefinitions implements SpecDefinitions {

    public static final String ERROR_CREATING_SPEC = "error trying to instantiate SpecDefintion";

    private final String prefix;
    private final PetriSupport petriSupport;

    public ClasspathSpecDefinitions(String packageToScan, PetriSupport petriSupport) {
        prefix = packageToScan;
        this.petriSupport = petriSupport;
    }

    @Override
    public List<SpecDefinition> get() {
        Reflections reflections = new Reflections(prefix);
        Set<Class<? extends SpecDefinition>> subTypes =
                reflections.getSubTypesOf(SpecDefinition.class);
        List<SpecDefinition> results = new ArrayList<SpecDefinition>();
        for (Class<? extends SpecDefinition> subType : subTypes) {
            try {
                results.add(subType.newInstance());
            } catch (Exception e) {
                petriSupport.report(ERROR_CREATING_SPEC, String.format("for type %s. Exception was - %s", subType, e.getMessage()), null);
            }
        }
        return results;
    }

}
