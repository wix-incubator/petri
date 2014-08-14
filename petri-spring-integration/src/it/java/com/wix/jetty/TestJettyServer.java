package com.wix.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:26 PM
 */
public class TestJettyServer {

    Server server;

    public TestJettyServer() throws Exception {

        startJetty();
    }

    private void startJetty() throws Exception {
        server = new Server(9002);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new UserInfoController()), "/hello/*");
        server.start();

    }

    public void stop() throws Exception {
        server.stop();
    }
}
