package com.wixpress.petri.petri;

import java.io.IOException;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface MappingErrorHandler {
    void handleError(String string, String entityDescription, IOException e);
}
