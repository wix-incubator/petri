package com.wixpress.guineapig.services;

public interface GuineaPigErrorHandler {

    void handle(Throwable exception, String action, Object source);

}
