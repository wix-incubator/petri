package com.wixpress.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.DefaultErrorResolver;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:54 AM
* To change this template use File | Settings | File Templates.
*/
public class JsonRPCServer {
    private Object rpc;
    private final Server server;

    public JsonRPCServer(Object serviceImpl, ObjectMapper objectMapper, int port, Class<?> remoteInterface) {
        this.rpc = serviceImpl;
        this.server = new Server(port);
        this.server.setHandler(new JsonRpcHandler(serviceImpl, objectMapper, remoteInterface));

    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    /**
    * Created with IntelliJ IDEA.
    * User: sagyr
    * Date: 9/1/14
    * Time: 5:15 PM
    * To change this template use File | Settings | File Templates.
    */
    public static class JsonRpcHandler extends AbstractHandler {
        private JsonRpcServer rpcServer;

        public JsonRpcHandler(Object rpc, final ObjectMapper objectMapper, Class<?> remoteInterface) {
            rpcServer = new JsonRpcServer(objectMapper,rpc, remoteInterface);
            rpcServer.setErrorResolver(new ExceptionSerializingErrorResolver(objectMapper));
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            rpcServer.handle(request, response);
            baseRequest.setHandled(true);
            response.setStatus(HttpServletResponse.SC_OK);

        }

        private static class ExceptionSerializingErrorResolver extends DefaultErrorResolver {
            private final ObjectMapper objectMapper;

            public ExceptionSerializingErrorResolver(ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
            }

            @Override
            public JsonError resolveError(Throwable t, Method method, List<JsonNode> arguments) {

                try {
                    return new JsonError(0, t.getMessage(),
                                new ErrorData(t.getClass().getName(), objectMapper.writeValueAsString(t)));
                } catch (JsonProcessingException e) {
                    return super.resolveError(t, method, arguments);
                }
            }

        }
    }
}
