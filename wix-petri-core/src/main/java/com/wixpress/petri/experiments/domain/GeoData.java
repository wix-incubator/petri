package com.wixpress.petri.experiments.domain;

import org.apache.commons.lang.StringUtils;

/**
 * @author matand
 * @since 1/7/13
 */
public class GeoData {

    String overrideCountryCode;
    String origCountryCode;

    public GeoData(String overrideCountryCode, String origCountryCode) {
        this.overrideCountryCode = overrideCountryCode;
        this.origCountryCode = origCountryCode;
    }

    public String getEffectiveCountryCode() {
        return StringUtils.defaultIfEmpty(
                overrideCountryCode,
                origCountryCode
        );
    }

}
