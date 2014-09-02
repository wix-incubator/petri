package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import com.wixpress.petri.petri.JodaTimeClock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

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

    @Bean
    public Laboratory laboratory(final UserInfoExtractor extractor) throws MalformedURLException {

        Experiments experiments = new CachedExperiments(new PetriClientExperimentSource());
        TestGroupAssignmentTracker tracker = new BILoggingTestGroupAssignmentTracker(new JodaTimeClock());
        // TODO: Implement userInfoStorage to read from cookie and write to cookie
        UserInfoStorage userInfoStorage = new UserInfoStorage() {
            @Override
            public void write(UserInfo info) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public UserInfo read() {
                return extractor.extract();
            }
        };
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
    public UserInfoExtractor userInfoExtractor(HttpServletRequest request) {
        return new HttpRequestUserInfoExtractor(request, new HostResolver());
    }

}
