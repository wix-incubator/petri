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

//    @Bean
//    public HttpInvokerServiceExporter petriServerExporter() {
//        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
//        exporter.setService(new RAMPetriClient());
//        exporter.setServiceInterface(PetriClient.class);
//        return exporter;
//    }


    @Bean
    public JsonServiceExporter petriServerExporter() {
        JsonServiceExporter exporter = new JsonServiceExporter();
        exporter.setService(new RAMPetriClient());
        exporter.setServiceInterface(PetriClient.class);
        exporter.setObjectMapper(ObjectMapperFactory.makeObjectMapper());
        return exporter;
    }


//    <!--<bean name="petriServerExporter"-->
//    <!--class="com.googlecode.jsonrpc4j.spring.JsonServiceExporter">-->
//    <!--<property name="service" ref="petriServerBean"/>-->
//    <!--<property name="serviceInterface" value="com.wixpress.petri.petri.PetriClient"/>-->
//    <!--<property name="objectMapper" ref="myObjectMapper"/>-->
//    <!--</bean>-->

//
//
//    <bean name = "petriServerBean" class = "com.wixpress.petri.petri.RAMPetriClient">
//    </bean>
//
//    <bean name = "myObjectMapper" class="com.fasterxml.jackson.databind.ObjectMapper" factory-bean="objectMapperFactory" />
//
//    <bean name = "objectMapperFactory" class = "com.wixpress.petri.experiments.jackson.ObjectMapperFactory" factory-method="makeObjectMapper"/>


}
