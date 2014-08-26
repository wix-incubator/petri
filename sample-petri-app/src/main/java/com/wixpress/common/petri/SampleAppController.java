package com.wixpress.common.petri;

import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.petri.SpecDefinition;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/25/14
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
public class SampleAppController {
    @Resource
    private Laboratory laboratory;

    public String conductExperiment(String key, String fallback) throws ClassNotFoundException {
        return laboratory.conductExperiment((Class<? extends SpecDefinition>) Class.forName(key),fallback);
    }

}
