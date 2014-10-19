package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import com.wixpress.petri.laboratory.http.LaboratoryFilter;
import com.wixpress.petri.petri.JodaTimeClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/27/14
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration

public class LaboratoryConfig {


    private @Value("${petri.url}") String petriUrl;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        final PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setLocation(new ClassPathResource("laboratory.properties"));
        return propertyPlaceholderConfigurer;
    }

    @Bean
    public Laboratory laboratory(UserInfoStorage userInfoStorage) throws MalformedURLException {

        Experiments experiments = new CachedExperiments(new PetriClientExperimentSource(petriUrl));
        TestGroupAssignmentTracker tracker = new BILoggingTestGroupAssignmentTracker(new JodaTimeClock());
        // TODO: implement file logging error handler
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public void handle(String message, Throwable cause) {
                cause.printStackTrace();
            }
        };
        return new TrackableLaboratory(experiments,tracker,userInfoStorage,errorHandler);
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public UserInfoStorage requestScopedUserInfoStorage(UserInfoExtractor extractor) {
        return new RequestScopedUserInfoStorage(extractor);
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public UserInfoExtractor userInfoExtractor(HttpServletRequest request) {
        return new HttpRequestUserInfoExtractor(request, new HostResolver());
    }

    @Bean
    public Filter laboratoryFilter(UserInfoStorage storage) {
        return new LaboratoryFilter(storage);
    }

}
