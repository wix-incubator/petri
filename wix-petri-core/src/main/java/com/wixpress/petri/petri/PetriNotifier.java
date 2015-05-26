package com.wixpress.petri.petri;

import javax.mail.internet.InternetAddress;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public interface PetriNotifier {
    void notify(String title, String message, String user);
    void notify(String title, String message, MailRecipients recipients, InternetAddress from);
}
