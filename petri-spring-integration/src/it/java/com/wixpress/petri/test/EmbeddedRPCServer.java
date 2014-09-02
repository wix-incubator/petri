package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.jetty.JsonRpcHandler;
import org.eclipse.jetty.server.Server;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:54 AM
* To change this template use File | Settings | File Templates.
*/
public class EmbeddedRPCServer {
    private Object rpc;
    private final Server server;

    public EmbeddedRPCServer(Object serviceImpl, ObjectMapper objectMapper, int port, Class<?> remoteInterface) {
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
}
