package com.wixpress.petri.laboratory;

/**
 * @author: talyag
 * @since: 3/22/14
 */
public interface ErrorHandler {
    void handle(String message, Throwable cause);
}
