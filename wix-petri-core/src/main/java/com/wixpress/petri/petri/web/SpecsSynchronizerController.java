package com.wixpress.petri.petri.web;

import com.wixpress.petri.petri.SpecsSynchronizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@Controller
@RequestMapping("/")
public class SpecsSynchronizerController {

    public static final String SPEC_DEFS_BASE_PACKAGE = "specs";

    @Autowired
    private SpecsSynchronizer specsSynchronizer;


    public SpecsSynchronizerController() {
    }

    @RequestMapping(value = "/sync-specs", method = RequestMethod.POST)
    @ResponseBody
    public List<String> syncSpecs() {
        return specsSynchronizer.syncSpecs();
    }

}
