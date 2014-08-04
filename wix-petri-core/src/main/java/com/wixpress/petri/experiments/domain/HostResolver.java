package com.wixpress.petri.experiments.domain;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: itayk
 * Date: 22/07/14
 */
public class HostResolver {

    public String resolve() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown host";
        }
    }
}
