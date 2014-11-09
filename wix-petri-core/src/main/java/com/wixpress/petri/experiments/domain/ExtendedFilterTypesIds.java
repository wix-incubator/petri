package com.wixpress.petri.experiments.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author talyag
 * @since 9/9/14
 */
public class ExtendedFilterTypesIds {
    private static Map<String, Class<? extends Filter>> extendedFilterTypeIds = new HashMap<>();

    public static void extendFilterTypeIds(String typeId, Class<? extends Filter> filterType) {
        extendedFilterTypeIds.put(typeId, filterType);
    }

    public static Set<Map.Entry<String, Class<? extends Filter>>> extendedTypes() {
        return extendedFilterTypeIds.entrySet();
    }
}
