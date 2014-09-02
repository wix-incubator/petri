package com.wixpress.petri.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.wixpress.petri.petri.PetriClient;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/1/14
* Time: 5:15 PM
* To change this template use File | Settings | File Templates.
*/
public class JsonRpcHandler extends AbstractHandler {
    private JsonRpcServer rpcServer;

    public JsonRpcHandler(Object rpc, ObjectMapper objectMapper, Class<?> remoteInterface) {
        rpcServer = new JsonRpcServer(objectMapper,rpc, remoteInterface);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        rpcServer.handle(request, response);
    }
}
