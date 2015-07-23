package com.wixpress.petri.experiments.domain;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ExtendedFilterTypesIds {
    private static Map<String, Class<? extends Filter>> extendedFilterTypeIds = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ExtendedFilterTypesIds.class);

    public static void extendFilterTypeIds(String typeId, Class<? extends Filter> filterType) {
        logger.info(String.format("adding extended filter type - %s, class - %s", typeId, filterType));
        extendedFilterTypeIds.put(typeId, filterType);
    }

    public static Map<String, Class<? extends Filter>> extendedTypes() {
        return ImmutableMap.copyOf(extendedFilterTypeIds);
    }
}
