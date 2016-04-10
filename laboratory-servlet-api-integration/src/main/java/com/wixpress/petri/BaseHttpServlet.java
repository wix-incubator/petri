package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.entity.ContentType;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Laurent_Gaertner
 * @since 11-Apr-2016
 */
public abstract class BaseHttpServlet extends HttpServlet {

    private void writeResponse(HttpServletResponse response, String value, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding(CharEncoding.UTF_8);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(value);
        response.getWriter().flush();
        response.getWriter().close();
    }

    protected void writeAsTextResponse(HttpServletResponse response, String value) throws IOException {
        writeResponse(response, value, ContentType.TEXT_PLAIN.getMimeType());
    }

    protected void writeAsJsonResponse(HttpServletResponse response, Object value) throws IOException {
        writeResponse(response, asJson(value), ContentType.APPLICATION_JSON.getMimeType());
    }

    protected String asJson(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter out = new StringWriter();
        mapper.writeValue(out, obj);
        return out.toString();
    }
}
