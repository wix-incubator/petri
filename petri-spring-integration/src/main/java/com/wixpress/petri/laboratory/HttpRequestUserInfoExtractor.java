package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 3:44 PM
 */
public class HttpRequestUserInfoExtractor implements UserInfoExtractor {

    private final HttpServletRequest request;
    private final HostResolver hostResolver;

    public HttpRequestUserInfoExtractor(HttpServletRequest request, HostResolver hostResolver) {
        this.request = request;

        this.hostResolver = hostResolver;
    }

    @Override
    public UserInfo extract() {

        String host = hostResolver.resolve();
        if (request == null) {
            return UserInfo.userInfoFromNullRequest(host);
        }

        String userAgent = sanitizeString(request.getHeader("user-agent"));
        boolean isRobot = checkForRobotHeader(userAgent);
        String url = getRequestURL();
        String ip = getIp();
        String language = request.getLocale().getLanguage();
        String country = getCountry();

        String experimentsLog = ""; //todo
        UUID userId = null; //todo
        UUID clientId = null; //todo

        UserInfoType userInfoType = UserInfoTypeFactory.make(null);

        DateTime userCreationDate = null; //todo
        String email = null; //todo
        String anonymousExperimentsLog = anonymousExperimentsLog();
        boolean isRecurring = false;  //todo
        Map<String, String> experimentOverrides = new HashMap<String, String>(); //todo
        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, userInfoType, language, country,
                userCreationDate, email, anonymousExperimentsLog, isRecurring, experimentOverrides, isRobot, host);
    }

    private String anonymousExperimentsLog() {
        final Cookie laboratoryCookie = WebUtils.getCookie(request, "_wixAB3");
        return laboratoryCookie == null ? "" : laboratoryCookie.getValue();
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

    String getCountry(){
        String geoCountry = request.getHeader("GEOIP_COUNTRY_CODE");
        if (geoCountry != null){
            return geoCountry;
        }
        return request.getLocale().getCountry();
    }

    String getIp(){
        String originatingIP = request.getHeader("X-FORWARDED-FOR");
        if (originatingIP != null){
            return originatingIP;
        }
        return request.getRemoteAddr();
    }

    String getRequestURL() {

        if (StringUtils.isNotEmpty(request.getParameter("appUrl")))
            return request.getParameter("appUrl");

        String URL = request.getRequestURL().toString();

        if (request.getServerName().equals("localhost")) {
            String serverName = request.getHeader("x-forwarded-host");
            if (serverName == null) serverName = request.getHeader("X_FORWARDED_HOST");

            if (serverName != null)
                URL = URL.replaceFirst("localhost", serverName);
        }

        return URL;
    }

}
