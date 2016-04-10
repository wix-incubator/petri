package com.wixpress.petri.laboratory;

public class DefaultErrorHandler implements ErrorHandler {

    @Override
    public void handle(String message, Throwable cause, ExceptionType exceptionType) {
        cause.printStackTrace();
    }
}
