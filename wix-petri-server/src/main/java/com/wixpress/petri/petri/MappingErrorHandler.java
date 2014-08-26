package com.wixpress.petri.petri;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/11/14
 * Time: 4:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MappingErrorHandler {
    void handleError(String string, String entityDescription, IOException e);
}
