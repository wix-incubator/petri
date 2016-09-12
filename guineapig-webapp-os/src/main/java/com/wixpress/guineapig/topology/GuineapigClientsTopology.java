package com.wixpress.guineapig.topology;

//@Configurable(namespace = "servicesTopology")
public class GuineapigClientsTopology implements GuineapigTopologyInterface {



//    @Configurable
    public String petriMailingList;

//    @Configurable
    private String mailServiceUrl;

//    @Configurable
    private String petriUrl;

    public String getPetriMailingList() {
        return petriMailingList;
    }

    public String getPetriUrl() {
        return petriUrl;
    }


    public String getMailServiceUrl() {
        return mailServiceUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuineapigClientsTopology that = (GuineapigClientsTopology) o;

        if (!mailServiceUrl.equals(that.mailServiceUrl)) return false;
        if (!petriMailingList.equals(that.petriMailingList)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = petriMailingList.hashCode();
        result = 31 * result + mailServiceUrl.hashCode();

        return result;
    }
}
