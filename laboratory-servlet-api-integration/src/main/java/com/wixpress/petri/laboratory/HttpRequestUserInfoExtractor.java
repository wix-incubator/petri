package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.HostResolver;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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

    private final String EXPERIMENTS_OVERRIDE_REQUEST_PARAM = "petri_ovr";
    private final String USER_ID_REQUEST_PARAM = "laboratory_user_id";
    private final HttpServletRequest request;
    private final String petriLogStorageCookieName;
    private final ExperimentOverridesUrlDecoder experimentOverridesUrlDecoder = new ExperimentOverridesUrlDecoder();

    public HttpRequestUserInfoExtractor(HttpServletRequest request, String petriLogStorageCookieName) {
        this.request = request;
        this.petriLogStorageCookieName = petriLogStorageCookieName;
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
        String language = request.getLocale().getLanguage();
        String country = getCountry();
        UUID userId = getUserId();
        UUID clientId = null;
        boolean isRecurringUser = clientId != null;
        String anonymousExperimentsLog = getCookieValue(petriLogStorageCookieName);
        UserInfoType userInfoType = UserInfoTypeFactory.make(userId);
        Map<String, String> experimentOverrides =
                experimentOverridesUrlDecoder.decode(request.getParameter(EXPERIMENTS_OVERRIDE_REQUEST_PARAM));


        String experimentsLog = "";
        if (!userInfoType.isAnonymous()) {
            experimentsLog = getExperimentsLog(petriLogStorageCookieName, userId);
        }

        DateTime userCreationDate = null; //todo
        boolean isCompanyEmployee = false; //todo
        boolean registeredUserExists = userId != null; // todo

        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, userInfoType, language, country,
                userCreationDate, isCompanyEmployee, anonymousExperimentsLog, isRecurringUser, experimentOverrides, isRobot, host, registeredUserExists);
    }

    private UUID getUserId() {
        String userId = request.getParameter(USER_ID_REQUEST_PARAM);
        return userId == null ? null : UUID.fromString(userId);
    }

    private String getExperimentsLog(String petriLogStorageCookieName, UUID userId) {
        if (userId != null) {
            return getCookieValue(petriLogStorageCookieName + "|" + userId.toString());
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

    String getCountry() {
        String geoCountry = request.getHeader("GEOIP_COUNTRY_CODE");
        if (geoCountry != null) {
            return geoCountry;
        }
        return request.getLocale().getCountry();
    }

    String getIp() {
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
