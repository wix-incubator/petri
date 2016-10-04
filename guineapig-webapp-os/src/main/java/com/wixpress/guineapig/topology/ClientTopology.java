package com.wixpress.guineapig.topology;

public class ClientTopology {

    public ClientTopology(){}

    public String staticPetriBaseUrl;
    public String redirectUri = "";
    public boolean useBoAuthenticationServer = false;
    public boolean production;

    public boolean isUseBoAuthenticationServer() {
        return useBoAuthenticationServer;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getStaticPetriBaseUrl() {
        return this.staticPetriBaseUrl;
    }
    public boolean isProduction() {
        return production;
    }

}
