package com.wixpress.guineapig.web;

import com.wixpress.guineapig.topology.AuthTopology;
import com.wixpress.guineapig.topology.ClientTopology;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebUiController extends BaseController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndex(Model model) throws Exception {

        ClientTopology clientTopology = new ClientTopology();
        clientTopology.redirectUri = "";

        AuthTopology auth = new AuthTopology("", "", "", "");

        model.addAttribute("staticsUrl", "/resources/statics/");
        model.addAttribute("clientTopology", clientTopology);
        model.addAttribute("auth", auth);
        return "index";
    }

}

