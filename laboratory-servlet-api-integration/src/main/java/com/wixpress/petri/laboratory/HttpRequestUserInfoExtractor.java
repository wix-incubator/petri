package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.HostResolver;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 3:44 PM
 */
public class HttpRequestUserInfoExtractor implements UserInfoExtractor {

    private final HttpServletRequest request;
    private final String petriCookieName;
    private FilterParametersConfig filterParametersConfig;
    private final ExperimentOverridesUrlDecoder experimentOverridesUrlDecoder = new ExperimentOverridesUrlDecoder();

    public HttpRequestUserInfoExtractor(HttpServletRequest request, String petriCookieName,
                                        FilterParametersConfig filterParametersConfig) {
        this.request = request;
        this.petriCookieName = petriCookieName;
        this.filterParametersConfig = filterParametersConfig;
    }

    @Override
    public UserInfo extract() {
        String host = HostResolver.getServerName();
        if (request == null) {
            return UserInfo.userInfoFromNullRequest(host);
        }

        String userAgent = sanitizeString(request.getHeader("user-agent"));
        boolean isRobot = checkForRobotHeader(userAgent);
        String url = getRequestURL();
        String ip = getIp();
        String language = new LanguageResolver().resolve(request, filterParametersConfig);
        String country = new CountryResolver().resolve(request, filterParametersConfig);
        UUID userId = new UserIdResolver().resolve(request, filterParametersConfig);
        String anonymousExperimentsLog = getCookieValue(petriCookieName);
        UserInfoType userInfoType = UserInfoTypeFactory.make(userId);
        Map<String, String> experimentOverrides =
                experimentOverridesUrlDecoder.decode(request.getParameter("petri_ovr"));


        String experimentsLog = "";
        if (!userInfoType.isAnonymous()) {
            experimentsLog = getExperimentsLog(petriCookieName, userId);
        }

        boolean registeredUserExists = userId != null; // todo

        return new UserInfo(experimentsLog, userId, null, ip, url, userAgent, userInfoType, language, country,
                null, false, anonymousExperimentsLog, false, experimentOverrides, isRobot, host, registeredUserExists);
    }

    private String getExperimentsLog(String petriCookieName, UUID userId) {
        if (userId != null) {
            return getCookieValue(petriCookieName + "|" + userId.toString());
        }
        return "";
    }

    private String getCookieValue(String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    private String sanitizeString(String someString) {
        return someString == null ? "" : someString;
    }

    boolean checkForRobotHeader(String userAgent) {
        userAgent = userAgent.toLowerCase();
        return (userAgent.contains("bot") ||
                userAgent.contains("crawler") ||
                userAgent.contains("spider") ||
                userAgent.contains("ping") ||
                userAgent.contains("nagios-plugins"));
    }

    private String getIp() {
        String originatingIP = request.getHeader("X-FORWARDED-FOR");
        if (originatingIP != null) {
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
