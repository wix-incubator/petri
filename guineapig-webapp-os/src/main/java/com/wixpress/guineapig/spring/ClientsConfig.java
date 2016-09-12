package com.wixpress.guineapig.spring;

import com.wixpress.guineapig.topology.GuineapigClientsTopology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * Date: 2/20/14
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */

@Configuration
public class ClientsConfig {

    @Bean
    public GuineapigClientsTopology servicesTopology()
    {
        return new GuineapigClientsTopology();
    }




}
