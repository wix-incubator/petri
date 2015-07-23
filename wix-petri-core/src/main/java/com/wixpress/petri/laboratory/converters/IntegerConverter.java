package com.wixpress.petri.laboratory.converters;


import com.wixpress.petri.laboratory.TestResultConverter;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */

public class IntegerConverter implements TestResultConverter<Integer> {

    @Override
    public Integer convert(String value) {
        return Integer.valueOf(value);
    }

}
