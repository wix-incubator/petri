package com.wixpress.guineapig.services;

/**
 * Created by avgarm on 7/29/2014.
 */
public class UnrecognizedFilterException extends RuntimeException {
    public UnrecognizedFilterException(String message) {
        super(message);
    }
}
