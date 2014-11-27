package com.wixpress.petri;

import static java.lang.String.format;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 11/27/14
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class NonSerializableServerException extends RuntimeException {
    public NonSerializableServerException(String exceptionTypeName, String serializedException) {
        super(format("Failed to serialize server exception of type: %s with content: %s" , exceptionTypeName, serializedException));
    }
}
