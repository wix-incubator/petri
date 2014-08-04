package com.wixpress.petri.laboratory.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 1/21/14
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestUtils {
    public static Map<String,String> mapOf(String... vals) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i=0; i<vals.length; i+=2) {
            result.put(vals[i],vals[i+1]);
        }
        return result;
    }

}
