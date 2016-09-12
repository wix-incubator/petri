package com.wixpress.guineapig.topology;

import com.google.common.collect.ImmutableMap;

import java.util.Map;


//@Configurable(namespace = "topology")
public class ServerTopology  {


    //@Configurable(optional = true, dynamic = true)
    protected Map<String, String> templatesFileSystemLocations;

    public Map<String, String> getTemplatesFileSystemLocations() {
        return (templatesFileSystemLocations != null ? templatesFileSystemLocations : ImmutableMap.<String, String>builder()
                .put("petri", "/var/www/static.wix.com/services/wix-petri-static/libs-releases-local/").build());
    }

}
