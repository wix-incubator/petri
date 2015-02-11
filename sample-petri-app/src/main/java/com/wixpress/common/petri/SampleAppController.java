package com.wixpress.common.petri;

import com.wixpress.petri.laboratory.ConductionContextBuilder;
import com.wixpress.petri.laboratory.EligibilityCriteriaTypes;
import com.wixpress.petri.laboratory.EligibilityCriteriaTypes.CustomContextCriterion;
import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.petri.SpecDefinition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @RequestMapping(value = "/conductExperiment", method = RequestMethod.GET)
    @ResponseBody
    public String conductExperiment(@RequestParam("key") String key, @RequestParam("fallback") String fallback) throws ClassNotFoundException {
        return laboratory.conductExperiment(key, fallback, new StringConverter());
    }

    @RequestMapping(value = "/conductExperimentWithCustomContext", method = RequestMethod.POST)
    @ResponseBody
    public String conductExperimentWithCustomContext(@RequestParam("key") String key, @RequestParam("fallback") String fallback,
                                                     @RequestBody final Map<String, String> customContext) throws ClassNotFoundException {
        return laboratory.conductExperiment(key, fallback, new StringConverter(),
                ConductionContextBuilder.newInstance().withCriterionOverride(new CustomContextCriterion(customContext)));
    }

    @RequestMapping(value = "/conductExperimentWithSpecDefinition", method = RequestMethod.GET)
    @ResponseBody
    public String conductExperimentWithSpecDefinition( @RequestParam("fallback") String fallback) throws ClassNotFoundException {
        return laboratory.conductExperiment(TestSpecDefinition.class, fallback, new StringConverter());
    }

}
