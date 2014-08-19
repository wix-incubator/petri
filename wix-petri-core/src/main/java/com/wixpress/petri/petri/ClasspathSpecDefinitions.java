package com.wixpress.petri.petri;

import com.wixpress.petri.laboratory.ErrorHandler;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author sagyr
 * @since 10/3/13
 */
public class ClasspathSpecDefinitions implements SpecDefinitions {

    public static final String ERROR_CREATING_SPEC = "error trying to instantiate SpecDefintion for type %s";

    private final String prefix;
    private final ErrorHandler errorHandler;

    public ClasspathSpecDefinitions(String packageToScan, ErrorHandler errorHandler) {
        prefix = packageToScan;
        this.errorHandler = errorHandler;
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
                errorHandler.handle(String.format(ERROR_CREATING_SPEC, subType), e);
            }
        }
        return results;
    }

}
