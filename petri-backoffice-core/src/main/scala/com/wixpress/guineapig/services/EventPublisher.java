package com.wixpress.guineapig.services;

public interface EventPublisher {
    void register(EventListener eventListener);
    void publish(ExperimentEvent experimentEvent);
    void reportError(Throwable exception,String action,Object source);
}
