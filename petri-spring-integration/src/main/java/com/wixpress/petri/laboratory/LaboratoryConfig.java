package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.JodaTimeClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;

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
    public UserInfoStorage requestScopedUserInfoStorage(HttpSession session) {
        return (UserInfoStorage) session.getAttribute(PETRI_USER_INFO_STORAGE);
    }

}
