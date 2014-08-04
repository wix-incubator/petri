package com.wixpress.petri.laboratory.converters;


import com.wixpress.petri.laboratory.TestResultConverter;

/**
* @author sagyr
* @since 10/20/13
*/

public class StringConverter implements TestResultConverter<String> {

    @Override
    public String convert(String value) {
        return value; // no-op
    }

}
