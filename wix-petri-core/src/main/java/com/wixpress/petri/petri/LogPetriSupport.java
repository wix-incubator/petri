package com.wixpress.petri.petri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public class LogPetriSupport implements PetriSupport {

    final Logger petriLog = LoggerFactory.getLogger(LogPetriSupport.class);

    @Override
    public void report(String title, String message, String user) {
        petriLog.debug(">>>>>>>>>>>>>>>>> " + title + " - " + message);
        petriLog.error(title + " - " + message);
    }


}
