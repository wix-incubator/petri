package com.wixpress.petri.petri;

import javax.mail.internet.InternetAddress;
import java.util.List;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public interface PetriNotifier {
    void notify(String title, String message, String... users);
    void notify(String title, String message, InternetAddress from, Boolean notifyCaptain,  List<String> recipients);
}
