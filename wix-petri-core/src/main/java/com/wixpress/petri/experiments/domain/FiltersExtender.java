package com.wixpress.petri.experiments.domain;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Set;

/**
 * @author talyag
 * @since 9/20/14
 */
public class FiltersExtender {

    public static void extendFilterTypes() {
        Reflections reflections = new Reflections("filters");
        Set<Class<?>> annonatedTypes = reflections.getTypesAnnotatedWith(FilterTypeName.class);
        for (Class<?> annonatedType : annonatedTypes) {
            Class<? extends Filter> filterSubType = (Class<? extends Filter>) annonatedType;
            String typeId = filterSubType.getAnnotation(FilterTypeName.class).value();
            ExtendedFilterTypesIds.extendFilterTypeIds(typeId, filterSubType);
        }
    }
}

