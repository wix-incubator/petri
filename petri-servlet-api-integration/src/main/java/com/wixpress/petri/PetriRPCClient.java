package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.FullPetriClient;
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
public class PetriRPCClient {

    public static JsonRpcHttpClient getJsonRpcHttpClient(String serviceUrl) throws MalformedURLException {
        final ObjectMapper mapper = ObjectMapperFactory.makeObjectMapper();
        JsonRpcHttpClient client = new JsonRpcHttpClient(mapper,
                new URL(serviceUrl),
                new HashMap<String, String>());

        client.setExceptionResolver(new DeserializingExceptionResolver(mapper));
        return client;
    }

    public static FullPetriClient makeFullClientFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = getJsonRpcHttpClient(serviceUrl);

        return ProxyUtil.createClientProxy(
                PetriRPCClient.class.getClassLoader(),
                FullPetriClient.class,
                client);
    }

    public static PetriClient makeFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = getJsonRpcHttpClient(serviceUrl);

        return ProxyUtil.createClientProxy(
                PetriRPCClient.class.getClassLoader(),
                PetriClient.class,
                client);
    }

    private static class DeserializingExceptionResolver implements ExceptionResolver {

        private final ObjectMapper objectMapper;

        public DeserializingExceptionResolver(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public Throwable resolveException(ObjectNode response) {
            ObjectNode errorObject = ObjectNode.class.cast(response.get("error"));
            ObjectNode dataObject = ObjectNode.class.cast(errorObject.get("data"));
            String exceptionTypeName = dataObject.get("exceptionTypeName").asText();
            String serializedException =  dataObject.get("message").asText();
            try {
                return Throwable.class.cast(objectMapper.readValue(serializedException, Class.forName(exceptionTypeName)));
            } catch (Exception e) {
                return new NonSerializableServerException(exceptionTypeName,serializedException);
            }
        }
    }
}
