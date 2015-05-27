package com.wixpress.petri.petri;

import org.junit.Before;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 1/15/14
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
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