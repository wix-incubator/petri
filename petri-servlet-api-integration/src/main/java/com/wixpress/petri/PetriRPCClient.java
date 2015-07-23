package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.UserRequestPetriClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class PetriRPCClient {

    private static final int userStateReadTimeoutMillis = 2000;

    public static JsonRpcHttpClient getJsonRpcHttpClient(String serviceUrl) throws MalformedURLException {
        final ObjectMapper mapper = ObjectMapperFactory.makeObjectMapper();
        JsonRpcHttpClient client = new JsonRpcHttpClient(mapper,
                new URL(serviceUrl),
                new HashMap<String, String>());

        client.setExceptionResolver(new DeserializingExceptionResolver(mapper));
        return client;
    }

    public static FullPetriClient makeFullClientFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = getJsonRpcHttpClient(serviceUrl+"/full_api");

        return ProxyUtil.createClientProxy(
                PetriRPCClient.class.getClassLoader(),
                FullPetriClient.class,
                client);
    }

    public static PetriClient makeFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = getJsonRpcHttpClient(serviceUrl+"/api");

        return ProxyUtil.createClientProxy(
                PetriRPCClient.class.getClassLoader(),
                PetriClient.class,
                client);
    }

    public static UserRequestPetriClient makeUserRequestFor(String serviceUrl) throws MalformedURLException {

        JsonRpcHttpClient client = getJsonRpcHttpClient(serviceUrl + "/user_request_api");
        client.setReadTimeoutMillis(userStateReadTimeoutMillis);
        client.setConnectionTimeoutMillis(userStateReadTimeoutMillis);

        return ProxyUtil.createClientProxy(
                PetriRPCClient.class.getClassLoader(),
                UserRequestPetriClient.class,
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
