package com.wixpress.guineapig.services;

import java.util.ArrayList;
import java.util.List;

public class ExperimentEventPublisher implements EventPublisher {
    List<EventListener> eventListeners = new ArrayList<>();
    final GuineaPigErrorHandler guineaPigErrorHandler;

    public ExperimentEventPublisher(GuineaPigErrorHandler guineaPigErrorHandler) {
        this.guineaPigErrorHandler = guineaPigErrorHandler;
    }

    @Override
    public void register(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public void publish(ExperimentEvent experimentEvent) {
        for (EventListener listener : eventListeners) {
            try {
                listener.onPublisherEventFire(experimentEvent);
            } catch (Throwable ex) {
                reportError(ex, experimentEvent.getAction(), listener);
            }
        }
    }

    @Override
    public void reportError(Throwable exception, String action, Object source) {
        guineaPigErrorHandler.handle(exception, action, source);
    }
}
