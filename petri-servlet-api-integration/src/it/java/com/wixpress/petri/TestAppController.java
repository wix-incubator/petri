package com.wixpress.petri;

import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoStorage;
import com.wixpress.petri.laboratory.converters.StringConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@Controller
public class TestAppController {

    @Resource
    private UserInfoStorage uiStorage;

    @Resource
    private Laboratory laboratory;

    @RequestMapping(value = "/extractUserInfo", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public UserInfo extractUserInfo() {
        return uiStorage.read();
    }

    @RequestMapping(value = "/conductExperiment", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String conductExperiment(@RequestParam("key") String key, @RequestParam("fallback") String fallback) {
        return laboratory.conductExperiment(key,fallback,new StringConverter());
    }

}
