package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/2/14
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonRPCServlet extends HttpServlet {

    private final JsonRpcServer rpcServer;

    public JsonRPCServlet(Object rpc, final ObjectMapper objectMapper, Class<?> remoteInterface) {
        rpcServer = new JsonRpcServer(objectMapper,rpc, remoteInterface);
        rpcServer.setErrorResolver(new ExceptionSerializingErrorResolver(objectMapper));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        rpcServer.handle(req,resp);
    }

}
