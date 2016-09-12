package com.wixpress.guineapig.web;

import com.wixpress.guineapig.services.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class MetaDataController extends BaseController {

    @Autowired
    MetaDataService metaDataService;


    @RequestMapping(value = "/geo", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult getGeoList() throws Exception {
        return success(metaDataService.getGeoList());
    }

    @RequestMapping(value = "/userAgentRegexes", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult getUserAgentRegexList() throws Exception {
        return success(metaDataService.getUserAgentRegexList());
    }

    @RequestMapping(value = "/userGroups", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult getUserGroupsList() throws Exception {
        return success(metaDataService.getUserGroupsList());
    }


    @RequestMapping(value = "/languages", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult getLanguageList() throws Exception {
        return success(metaDataService.getLangList());
    }

    @RequestMapping(value = "/productmap", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult getScopeToSpecMap() throws Exception {
        return success(metaDataService.createScopeToSpecMap());
    }

}
