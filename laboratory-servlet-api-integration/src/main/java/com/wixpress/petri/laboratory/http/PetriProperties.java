package com.wixpress.petri.laboratory.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
* User: itayk
* Date: 06/11/14
*/
public class PetriProperties {

    public Properties fromStream(InputStream input) {
        // TODO - Unit test the !@#$ out of this
        Properties p = new Properties();
        try {
            p.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }
}
