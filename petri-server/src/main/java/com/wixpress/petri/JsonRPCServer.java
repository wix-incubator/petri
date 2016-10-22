package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.guineapig.web.GuineaPigDispatcherServlet$;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.PetriRpcServer;
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
public class JsonRPCServer  {
    private final Server server;
    public final ServletContextHandler context;

    public JsonRPCServer(PetriRpcServer serviceImpl, ObjectMapper objectMapper, int port, Boolean addBackOfficeWebapp) {
        this.server = new Server(port);
        context = createContext(addBackOfficeWebapp);
        context.setContextPath("/");
        server.setHandler(context);

        addServlets(serviceImpl, objectMapper, context);
    }

    private ServletContextHandler createContext(Boolean addBackOfficeWebapp) {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        if (addBackOfficeWebapp){
            GuineaPigDispatcherServlet$.MODULE$.addGuineaPigServlet(servletContextHandler);
        }
        return servletContextHandler;
    }

    private void addServlets(PetriRpcServer serviceImpl, ObjectMapper objectMapper, ServletContextHandler context) {

        context.addServlet(new ServletHolder(new JsonRPCServlet(serviceImpl,objectMapper, FullPetriClient.class)),"/petri/full_api");
        context.addServlet(new ServletHolder(new JsonRPCServlet(serviceImpl,objectMapper, PetriClient.class)),"/petri/api");
        context.addServlet(new ServletHolder(new JsonRPCServlet(serviceImpl,objectMapper, UserRequestPetriClient.class)),"/petri/user_request_api");
    }

    public void start() throws Exception {
        server.start();
        // TODO: rpcServer.join() -> In order to do this run the server on a different thread when testing.
    }

    public void stop() throws Exception {
        server.stop();
    }


}
