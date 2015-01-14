package com.wixpress.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.DefaultErrorResolver;

import java.lang.reflect.Method;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 10/2/14
* Time: 4:05 PM
* To change this template use File | Settings | File Templates.
*/
public class ExceptionSerializingErrorResolver extends DefaultErrorResolver {
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
