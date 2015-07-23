package com.wixpress.petri.laboratory;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface ErrorHandler {
    void handle(String message, Throwable cause, ExceptionType exceptionType);
}
