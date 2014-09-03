package com.wixpress.common.petri;

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.RAMPetriClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/25/14
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
public class PetriServerSpringConfig {


    @Bean
    public JsonServiceExporter petriServerExporter() {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(new RAMPetriClient());
        exporter.setServiceInterface(PetriClient.class);
        exporter.setObjectMapper(ObjectMapperFactory.makeObjectMapper());
        return exporter;
    }

}
