package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.servlet.http.HttpServletRequest;

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
    public Laboratory laboratory(UserInfoExtractor extractor) {
        //TODO: This is where we need to assemble TrackableLaboratory
        // UserInfoStorage should be a request scoped
        return null;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public UserInfoExtractor userInfoExtractor(HttpServletRequest request) {
        return new HttpRequestUserInfoExtractor(request, new HostResolver());
    }

}
