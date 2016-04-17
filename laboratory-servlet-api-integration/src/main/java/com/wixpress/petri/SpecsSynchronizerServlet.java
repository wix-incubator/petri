package com.wixpress.petri;

import com.wixpress.petri.laboratory.DefaultErrorHandler;
import com.wixpress.petri.laboratory.http.DefaultLaboratoryProperties;
import com.wixpress.petri.laboratory.http.LaboratoryProperties;
import com.wixpress.petri.petri.ClasspathSpecDefinitions;
import com.wixpress.petri.petri.JodaTimeClock;
import com.wixpress.petri.petri.SpecsSynchronizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * @author Laurent_Gaertner
 * @since 11-Apr-2016
 */
public class SpecsSynchronizerServlet extends BaseHttpServlet {

    private static final String PETRI_SPECS_PKG_KEY = "petri.specs.pkg";
    private static final String PETRI_URL_KEY = "petri.url";


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LaboratoryProperties laboratoryProperties = new DefaultLaboratoryProperties(getServletContext());

        ClasspathSpecDefinitions specDefinitions =
                new ClasspathSpecDefinitions(laboratoryProperties.getProperty(PETRI_SPECS_PKG_KEY), new DefaultErrorHandler());

        SpecsSynchronizer specsSynchronizer =
                new SpecsSynchronizer(PetriRPCClient.makeFor(laboratoryProperties.getProperty(PETRI_URL_KEY)), specDefinitions, new JodaTimeClock());

        List<String> specs = specsSynchronizer.syncSpecs();
        writeAsJsonResponse(response, specs);
    }

}
