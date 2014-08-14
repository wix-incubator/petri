package com.wixpress.petri.jetty;

import com.wixpress.petri.experiments.domain.HostResolver;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoExtractor;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.wixpress.petri.jackson.ObjectMapperFactory.getObjectMapper;

/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:31 PM
 */
public class UserInfoController  extends HttpServlet {
    ObjectMapper objectMapper =  getObjectMapper();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UserInfoExtractor userInfoExtractor = new UserInfoExtractor(request, new HostResolver());

        UserInfo userInfo = userInfoExtractor.extract();


        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(objectMapper.writeValueAsString(userInfo));

    }
}
