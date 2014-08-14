package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
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

        String host = hostResolver.resolve();
        if (request == null) {
            return UserInfo.userInfoFromNullRequest(host);
        }

        String userAgent = sanitizeString(request.getHeader("user-agent"));
        boolean isRobot = checkForRobotHeader(userAgent);


        String experimentsLog = null; //todo
        UUID userId = null; //todo
        UUID clientId = null; //todo
        String ip = null;   //todo
        String url = null;   //todo
        UserInfoType userInfoType = null; //todo
        String language = null;  //todo
        String country = null; //todo
        DateTime userCreationDate = null; //todo
        String email = null; //todo
        String anonymousExperimentsLog = null; //todo
        boolean isRecurring = false;  //todo
        Map<String, String> experimentOverrides = null; //todo
        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, userInfoType, language, country,
                userCreationDate, email, anonymousExperimentsLog, isRecurring, experimentOverrides, isRobot, host);

    }

    private String sanitizeString(String someString) {
        return someString == null ? "" : someString;
    }

    boolean checkForRobotHeader(String userAgent){
        userAgent = userAgent.toLowerCase();
        return (userAgent.contains("bot") ||
                userAgent.contains("crawler") ||
                userAgent.contains("spider") ||
                userAgent.contains("ping") ||
                userAgent.contains("nagios-plugins"));
    }
}
