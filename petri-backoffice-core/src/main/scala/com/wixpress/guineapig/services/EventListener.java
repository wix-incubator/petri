package com.wixpress.guineapig.services;

public interface EventListener {
    void onPublisherEventFire(ExperimentEvent experimentEvent) throws Exception;
}
