package com.wixpress.common.petri;

import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.petri.SpecDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;

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

}
