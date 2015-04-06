package com.wixpress.petri;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by talyag on 4/6/15.
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
