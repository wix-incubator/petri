package com.wixpress.common.petri.testutils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 8/27/14
* Time: 4:09 PM
* To change this template use File | Settings | File Templates.
*/
public class ServerRunner {

    private final Server server;

    public ServerRunner(int port, String pathToWebapp) {
        this.server = new Server(port);
        WebAppContext context = new WebAppContext();

        context.setContextPath("/");
        context.setParentLoaderPriority(true);
        context.setResourceBase(pathToWebapp);
        server.setHandler(context);

    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
