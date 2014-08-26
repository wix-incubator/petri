package com.wixpress.common.petri.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.SpecDefinition;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.joda.time.DateTime;
import org.junit.Test;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PetriE2eTest {

    @Test
    public void conductingASimpleExperiment() throws Exception {
        PetriServerDriver petriDriver = new PetriServerDriver(9010, petriServerResourceBase());
        petriDriver.start();
        PetriServerDriver sampleAppDriver = new PetriServerDriver(9011, sampleAppResourceBase());
        sampleAppDriver.start();

        PetriClient petriClient = PetriServerProxy.makeFor("http://localhost:9010/wix/petri");

        final ScopeDefinition scopeDefinition = new ScopeDefinition("the scope", false);
        final ExperimentSpec spec = SpecDefinition.ExperimentSpecBuilder.
                aNewlyGeneratedExperimentSpec("THE_KEY").
                withTestGroups(asList("a", "b")).
                withScopes(scopeDefinition).build();

        petriClient.addSpecs(asList(spec));
        final ExperimentSnapshot snapshot = ExperimentSnapshotBuilder.anExperimentSnapshot().
                withKey("THE_KEY").
                withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                withOnlyForLoggedInUsers(false).build();
        petriClient.insertExperiment(snapshot);

        // TODO: Flash out the rest of the test once the real server is integrated
        assertNotNull(petriClient.fetchActiveExperiments());
        sampleAppDriver.stop();
        petriDriver.stop();
    }


    public static class PetriServerProxy {
        public static PetriClient makeFor(String serviceUrl) throws MalformedURLException {
//            HttpInvokerProxyFactoryBean proxyFactory = new HttpInvokerProxyFactoryBean();
//            proxyFactory.setServiceUrl(serviceUrl);
//            proxyFactory.setServiceInterface(PetriClient.class);
//            proxyFactory.afterPropertiesSet();
//            return (PetriClient) proxyFactory.getObject();

            JsonRpcHttpClient client = new JsonRpcHttpClient(ObjectMapperFactory.makeObjectMapper(),
                    new URL(serviceUrl),
                    new HashMap<String, String>());


            return ProxyUtil.createClientProxy(
                    PetriServerProxy.class.getClassLoader(),
                    PetriClient.class,
                    client);

        }

    }

    public static class PetriServerDriver {

        private final Server petriServer;
        private final String pathToWebapp;

        public PetriServerDriver(int port, String pathToWebapp) {
            this.petriServer = new Server(port);
            this.pathToWebapp = pathToWebapp;
            WebAppContext context = new WebAppContext();

            context.setContextPath("/");
            context.setParentLoaderPriority(true);
            context.setResourceBase(pathToWebapp);
            petriServer.setHandler(context);

        }

        public void start() throws Exception {
            petriServer.start();
        }

        public void stop() throws Exception {
            petriServer.stop();
        }
    }

    private String petriServerResourceBase() {
        return getClass().getResource("/").getPath() + "../../../wix-petri-server/src/main/webapp";
    }

    private String sampleAppResourceBase() {
        return getClass().getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    }

}
