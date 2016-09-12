package com.wixpress.guineapig.web;

import com.wixpress.guineapig.entities.ui.UiSpec;
import com.wixpress.guineapig.services.SpecService;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


@Controller
public class SpecsController extends BaseController {

    @Autowired
    SpecService specService;

    @RequestMapping(value = "/Specs", method = RequestMethod.GET)
    @ResponseBody
    public GuineapigResult<List<UiSpec>> getAllSpecs() throws Exception {
        return success(specService.getAllSpecs());
    }

    @RequestMapping(value = "/addSpecs", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public GuineapigResult<Boolean> addSpec(@RequestBody ExperimentSpec[] experimentSpecs) throws Exception {
        try {
            specService.addSpecs(Arrays.asList(experimentSpecs));
            return success(true);
        } catch (Exception e) {
            return success(false);
        }
    }

    @RequestMapping(value = "/deleteSpecs/{specKey:.+}", method = RequestMethod.POST)
    @ResponseBody
    public GuineapigResult deleteSpec(@PathVariable("specKey") String specKey) throws Exception {
        try {
            specService.deleteSpec(specKey);
            return success(true);
        } catch (Exception e) {
            return success(false);
        }
    }

}
