package com.wix.jetty;

import com.wixpress.petri.laboratory.NullUserInfoType;
import com.wixpress.petri.laboratory.UserInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static com.wix.jackson.ObjectMapperFactory.getObjectMapper;

/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:31 PM
 */
public class UserInfoController  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ObjectMapper objectMapper =  getObjectMapper();

        UserInfo userInfo = new UserInfo("log", UUID.randomUUID() , UUID.randomUUID(), "ip", "url", "userAgent",
                new NullUserInfoType(), "language", "country", new DateTime(), "email", "anonymousExperimentsLog",
                false, new HashMap<String, String>(),  false, "host") ;

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(objectMapper.writeValueAsString(userInfo));
//        response.getWriter().println("{\"name\":\"ddd\"}");

    }
}
