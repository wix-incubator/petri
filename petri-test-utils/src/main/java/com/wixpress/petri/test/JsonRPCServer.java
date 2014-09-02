package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:54 AM
* To change this template use File | Settings | File Templates.
*/
public class JsonRPCServer {
    private Object rpc;
    private final Server server;

    public JsonRPCServer(Object serviceImpl, ObjectMapper objectMapper, int port, Class<?> remoteInterface) {
        this.rpc = serviceImpl;
        server = new Server(port);
        server.setHandler(new JsonRpcHandler(serviceImpl, objectMapper, remoteInterface));
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    /**
    * Created with IntelliJ IDEA.
    * User: sagyr
    * Date: 9/1/14
    * Time: 5:15 PM
    * To change this template use File | Settings | File Templates.
    */
    public static class JsonRpcHandler extends AbstractHandler {
        private JsonRpcServer rpcServer;

        public JsonRpcHandler(Object rpc, ObjectMapper objectMapper, Class<?> remoteInterface) {
            rpcServer = new JsonRpcServer(objectMapper,rpc, remoteInterface);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            rpcServer.handle(request, response);
        }
    }
}
