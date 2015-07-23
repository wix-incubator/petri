package com.wixpress.petri.petri;

import org.junit.Before;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class RAMPetriClientTest extends PetriClientContractTest {

    private RAMPetriClient ramPetriClient;

    @Before
    public void setup() {
        ramPetriClient = new RAMPetriClient();
    }

    @Override
    protected FullPetriClient fullPetriClient() {
        return ramPetriClient;
    }

    @Override
    protected PetriClient petriClient() {
        return ramPetriClient;
    }

    @Override
    protected UserRequestPetriClient synchPetriClient() {
        return ramPetriClient;
    }

}