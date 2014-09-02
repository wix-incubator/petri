package com.wixpress.petri;

import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoExtractor;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.petri.SpecDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/27/14
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
public class TestAppController {

    @Resource
    private UserInfoExtractor uiExtractor;

    @Resource
    private Laboratory laboratory;

    @RequestMapping(value = "/extractUserInfo", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public UserInfo extractUserInfo() {
        final UserInfo extract = uiExtractor.extract();
        return extract;
    }

    @RequestMapping(value = "/conductExperiment", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String conductExperiment(@RequestParam("key") String key, @RequestParam("fallback") String fallback) {
        return laboratory.conductExperiment(key,fallback,new StringConverter());
    }

}
