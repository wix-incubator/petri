package com.wixpress.common.petri.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created with IntelliJ IDEA.
 * User: arielw
 * Date: 24/10/12
 * Time: 12:29
 * Embedded Jetty server for servlets
 */
public class JettyLocalServletServer {

    private Server server = null;

    public JettyLocalServletServer() {
    }

    public void startServer(int port, String contextPath, String resourceBase) throws Exception {
        server = new Server(port);
        server.setStopAtShutdown(true);


        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(contextPath);
        webAppContext.setResourceBase(resourceBase);
        webAppContext.setClassLoader(getClass().getClassLoader());
        server.setHandler(webAppContext);

        server.start();

    }

    public void stopServer() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }

    }
}
