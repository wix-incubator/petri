package com.wixpress.petri.petri;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author: talyag
 * @since: 10/30/13
 */
public interface PetriMapper<T> extends RowMapper<T> {
    <T> String serialize(T obj) throws JsonProcessingException;

    <T> T deserialize(String string, TypeReference typeRef, String entityDescription);
}
