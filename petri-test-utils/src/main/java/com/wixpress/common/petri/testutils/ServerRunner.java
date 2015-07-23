package com.wixpress.common.petri.testutils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ServerRunner {

    private final Server server;
    private final String pathToWebapp;

    public ServerRunner(int port, String pathToWebapp) {
        this.server = new Server(port);
        this.pathToWebapp = pathToWebapp;
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
