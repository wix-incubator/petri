package com.wixpress.guineapig.topology;

//@Configurable(namespace = "clientTopology")
public class ClientTopology {

    public ClientTopology(){}

//    @Configurable
    public String petriHome;

//    @Configurable
    public String staticPetriBaseUrl;

//    @Configurable
    public String petriApiUrl;

//    @Configurable
    public String petriPartialsUrl;


//    @Configurable
    public String redirectUri;


    public boolean useBoAuthenticationServer = false;

    public boolean isUseBoAuthenticationServer() {
        return useBoAuthenticationServer;
    }

    public void setUseBoAuthenticationServer(boolean useBoAuthenticationServer) {
        this.useBoAuthenticationServer = useBoAuthenticationServer;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

//    @Configurable
    public  boolean production;

    public String getPetriHome() {
        return petriHome;
    }

    public String getStaticPetriBaseUrl() {
        return this.staticPetriBaseUrl;
    }

    public String getPetriApiUrl() {
        return petriApiUrl;
    }

    public String getPetriPartialsUrl() {
        return petriPartialsUrl;
    }

    public boolean isProduction() {
        return production;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientTopology that = (ClientTopology) o;

        if (useBoAuthenticationServer != that.useBoAuthenticationServer) return false;
        if (production != that.production) return false;
        if (petriHome != null ? !petriHome.equals(that.petriHome) : that.petriHome != null) return false;
        if (staticPetriBaseUrl != null ? !staticPetriBaseUrl.equals(that.staticPetriBaseUrl) : that.staticPetriBaseUrl != null)
            return false;
        if (petriApiUrl != null ? !petriApiUrl.equals(that.petriApiUrl) : that.petriApiUrl != null) return false;
        if (petriPartialsUrl != null ? !petriPartialsUrl.equals(that.petriPartialsUrl) : that.petriPartialsUrl != null)
            return false;
        return !(redirectUri != null ? !redirectUri.equals(that.redirectUri) : that.redirectUri != null);

    }

    @Override
    public int hashCode() {
        int result = petriHome != null ? petriHome.hashCode() : 0;
        result = 31 * result + (staticPetriBaseUrl != null ? staticPetriBaseUrl.hashCode() : 0);
        result = 31 * result + (petriApiUrl != null ? petriApiUrl.hashCode() : 0);
        result = 31 * result + (petriPartialsUrl != null ? petriPartialsUrl.hashCode() : 0);
        result = 31 * result + (redirectUri != null ? redirectUri.hashCode() : 0);
        result = 31 * result + (useBoAuthenticationServer ? 1 : 0);
        result = 31 * result + (production ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientTopology{" +
                ", petriHome='" + petriHome + '\'' +
                ", staticPetriBaseUrl='" + staticPetriBaseUrl + '\'' +
                ", petriApiUrl='" + petriApiUrl + '\'' +
                ", petriPartialsUrl='" + petriPartialsUrl + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                ", useBoAuthenticationServer=" + useBoAuthenticationServer +
                ", production=" + production +
                '}';
    }
}
