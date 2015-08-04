package com.wixpress.petri.fakeserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.UserRequestPetriClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:54 AM
* To change this template use File | Settings | File Templates.
*/
public class TestJsonRPCServer {
    private final Server server;

    public TestJsonRPCServer(Object serviceImpl, ObjectMapper objectMapper, int port) {
        this.server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new TestJsonRPCServlet(serviceImpl,objectMapper, PetriClient.class)),"/petri/api");
        context.addServlet(new ServletHolder(new TestJsonRPCServlet(serviceImpl,objectMapper, UserRequestPetriClient.class)),"/petri/user_request_api");

    }

    public void start() throws Exception {
        server.start();
        // TODO: rpcServer.join() -> In order to do this run the server on a different thread when testing.
    }

    public void stop() throws Exception {
        server.stop();
    }

}
