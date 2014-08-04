package com.wixpress.petri.petri;

/**
 * @author: talyag
 * @since: 9/30/13
 */
//TODO - change so a SupportMessage will decide how to notify of itself
// (the log message only has 'message', the email message has all 3 params)
public interface PetriSupport {
    void report(String title, String message, String user);
}
