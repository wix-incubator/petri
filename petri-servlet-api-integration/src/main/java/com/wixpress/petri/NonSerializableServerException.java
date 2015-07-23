package com.wixpress.petri;

import static java.lang.String.format;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class NonSerializableServerException extends RuntimeException {
    public NonSerializableServerException(String exceptionTypeName, String serializedException) {
        super(format("Failed to serialize server exception of type: %s with content: %s" , exceptionTypeName, serializedException));
    }
}
