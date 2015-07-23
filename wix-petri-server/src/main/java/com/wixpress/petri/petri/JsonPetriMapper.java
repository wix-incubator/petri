package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public abstract class JsonPetriMapper<T> implements PetriMapper<T> {

    protected final MappingErrorHandler mappingErrorHandler;
    protected ObjectMapper objectMapper;

    public JsonPetriMapper(ObjectMapper objectMapper, MappingErrorHandler mappingErrorHandler) {
        this.objectMapper = objectMapper;
        this.mappingErrorHandler = mappingErrorHandler;
    }

    @Override
    public <T> String serialize(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    @Override
    public <T> T deserialize(String string, TypeReference typeRef, String entityDescription) {
        try {
            return objectMapper.readValue(string, typeRef);
        } catch (IOException e) {
            mappingErrorHandler.handleError(string, entityDescription, e);
            return null;
        }
    }

}
