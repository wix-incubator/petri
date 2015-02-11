package com.wixpress.petri.experiments.domain;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author talyag
 * @since 9/20/14
 */
public class FiltersExtender {
    private static final Logger logger = LoggerFactory.getLogger(FiltersExtender.class);

    private static void registerFilter(Class<?> loadedFilterClass ) {
        FilterTypeName annotation = loadedFilterClass.getAnnotation(FilterTypeName.class);
        if (annotation != null) {
            ExtendedFilterTypesIds.extendFilterTypeIds(
                    annotation.value(), (Class<? extends Filter>) loadedFilterClass);
        }
    }

    public static void extendFilterTypes() {
        logger.info("scanning filters on classpath");
        Reflections reflections = new Reflections("filters");
        Set<Class<?>> annonatedTypes = reflections.getTypesAnnotatedWith(FilterTypeName.class);
        for (Class<?> annonatedType : annonatedTypes) {
            registerFilter(annonatedType);
        }
    }


    public static void dynamicallyLoadFilterTypes(String filtersPackage) {
        DynamicFilterLoader dynamicFilterLoader = new DynamicFilterLoader(filtersPackage);
        List<Class> filterToRegister = dynamicFilterLoader.loadFilterTypesFromJars();
        for (Class filterClass : filterToRegister){
            registerFilter(filterClass);
        }
    }


}

