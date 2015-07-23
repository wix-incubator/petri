package com.wixpress.petri.petri;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class HostResolver {

    static String serverName = null;

    public static String getServerName() {
        if (serverName == null) {
            try {
                serverName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                serverName = "unknown host";
            }
        }
        return serverName;

    }
}
