package com.wixpress.petri.petri;

import javax.mail.internet.InternetAddress;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface PetriNotifier {
    void notify(String title, String message, String user);
    void notify(String title, String message, MailRecipients recipients, InternetAddress from);
}
