package com.wixpress.common.petri;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.PetriClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/1/14
* Time: 2:25 PM
* To change this template use File | Settings | File Templates.
*/
public class PetriServerProxy {
    public static PetriClient makeFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = new JsonRpcHttpClient(ObjectMapperFactory.makeObjectMapper(),
                new URL(serviceUrl),
                new HashMap<String, String>());

        return ProxyUtil.createClientProxy(
                PetriServerProxy.class.getClassLoader(),
                PetriClient.class,
                client);
    }

}
