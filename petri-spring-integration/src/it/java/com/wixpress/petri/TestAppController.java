package com.wixpress.petri;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @RequestMapping(value = "/extractUserInfo", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public UserInfo conductExperiment() {
        final UserInfo extract = uiExtractor.extract();
        return extract;
    }

}
