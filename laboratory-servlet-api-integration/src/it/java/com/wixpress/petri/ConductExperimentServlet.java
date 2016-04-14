package com.wixpress.petri;

import com.wixpress.petri.laboratory.Laboratory;
import com.wixpress.petri.laboratory.converters.StringConverter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_LABORATORY;

/**
 * @author Laurent_Gaertner
 * @since 11-Apr-2016
 */
public class ConductExperimentServlet extends BaseHttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Laboratory laboratory = (Laboratory) session.getAttribute(PETRI_LABORATORY);

        String key = request.getParameter("key");
        String fallback = request.getParameter("fallback");
        String res = laboratory.conductExperiment(key, fallback, new StringConverter());

        writeAsTextResponse(response, res);
    }
}