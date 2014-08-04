package com.wixpress.petri.laboratory.converters;


import com.wixpress.petri.laboratory.TestResultConverter;

/**
* @author sagyr
* @since 10/20/13
*/

public class IntegerConverter implements TestResultConverter<Integer> {

    @Override
    public Integer convert(String value) {
        return Integer.valueOf(value);
    }

}
