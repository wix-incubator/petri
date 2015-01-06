package com.wixpress.petri.petri;

import com.google.common.base.Predicate;
import com.wixpress.petri.laboratory.ErrorHandler;
import com.wixpress.petri.laboratory.ExceptionType;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.filter;

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
        Iterable<Class<? extends SpecDefinition>> nonAbstractSubtypes =
                filter(reflections.getSubTypesOf(SpecDefinition.class), NonAbstractClassPredicate);

        List<SpecDefinition> results = new ArrayList<>();
        for (Class<? extends SpecDefinition> subType : nonAbstractSubtypes) {
            try {

                results.add(subType.newInstance());
            } catch (Exception e) {
                errorHandler.handle(String.format(ERROR_CREATING_SPEC, subType), e, ExceptionType.SpecScannerException);
            }
        }
        return results;
    }

    private static final Predicate<Class<? extends SpecDefinition>> NonAbstractClassPredicate = new Predicate<Class<? extends SpecDefinition>>() {
        @Override
        public boolean apply(@Nullable Class<? extends SpecDefinition> input) {
            return !Modifier.isAbstract(input.getModifiers());
        }
    };

}
