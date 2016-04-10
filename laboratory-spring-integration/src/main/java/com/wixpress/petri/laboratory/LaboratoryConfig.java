package com.wixpress.petri.laboratory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.servlet.http.HttpSession;

import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_LABORATORY;
import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_USER_INFO_STORAGE;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/27/14
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
public class LaboratoryConfig {

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Laboratory laboratory(HttpSession session) {
        return (Laboratory) session.getAttribute(PETRI_LABORATORY);
    }
}
