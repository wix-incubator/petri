package com.wixpress.petri.laboratory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.servlet.http.HttpSession;

import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_LABORATORY;
import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_USER_INFO_STORAGE;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@Configuration
public class LaboratoryConfig {

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Laboratory laboratory(HttpSession session) {
        return (Laboratory) session.getAttribute(PETRI_LABORATORY);
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public UserInfoStorage requestScopedUserInfoStorage(HttpSession session) {
        return (UserInfoStorage) session.getAttribute(PETRI_USER_INFO_STORAGE);
    }

}
