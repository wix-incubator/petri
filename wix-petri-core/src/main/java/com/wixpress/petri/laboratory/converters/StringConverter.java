package com.wixpress.petri.laboratory.converters;


import com.wixpress.petri.laboratory.TestResultConverter;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class StringConverter implements TestResultConverter<String> {

    @Override
    public String convert(String value) {
        return value; // no-op
    }

}
