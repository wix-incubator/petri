package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 3:44 PM
 */
public class UserInfoExtractor {

    private final HttpServletRequest request;
    private final HostResolver hostResolver;

    public UserInfoExtractor(HttpServletRequest request, HostResolver hostResolver) {
        this.request = request;

        this.hostResolver = hostResolver;
    }

    public UserInfo extract() {

        String experimentsLog = null;
        UUID userId = null;
        UUID clientId = null;
        String ip = null;
        String url = null;
        String userAgent = null;
        UserInfoType userInfoType = null;
        String language = null;
        String country = null;
        DateTime userCreationDate = null;
        String email = null;
        String anonymousExperimentsLog = null;
        boolean isRecurring = false;  //todo
        Map<String, String> experimentOverrides = null;
        boolean isRobot = false; //todo
        String host = hostResolver.resolve();
        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, userInfoType, language, country,
                userCreationDate, email, anonymousExperimentsLog, isRecurring, experimentOverrides, isRobot, host);

}
}
