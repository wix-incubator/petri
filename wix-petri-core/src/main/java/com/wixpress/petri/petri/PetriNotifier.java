package com.wixpress.petri.petri;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public interface PetriNotifier {
    void notify(String title, String message, String... users);
    void notify(String title, String message, Boolean notifyCaptain,  List<String> recipients);
}
