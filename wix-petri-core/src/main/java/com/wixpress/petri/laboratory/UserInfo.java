package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.wixpress.petri.laboratory.UserInfoType.ANONYMOUS_LOG_STORAGE_KEY;

public class UserInfo implements TestGroupDrawer {

    private static final DateTime BEGINNING_OF_TIME = new DateTime(0);

    public static UserInfo userInfoFromNullRequest(String host) {
        return new UserInfo("", (UUID) null, null, "", "", "",
                new NullUserInfoType(), "", "", BEGINNING_OF_TIME, "", "", false, new HashMap<String, String>(), false, host, null);
    }

    public final String experimentsLog;
    private final UserInfoType type;
    public final UUID clientId;
    public final String ip;
    public final String url;
    public final String userAgent;
    private final UUID userId;
    public final String language;
    public final String country;
    public final DateTime dateCreated;
    public final String email;
    public final String anonymousExperimentsLog;
    public final boolean isRecurringUser;
    public final Map<String, String> experimentOverrides;
    public final boolean isRobot;
    public final String host;
    public final BrowserVersion browserVersion;

    public UserInfo(String experimentsLog, UUID userId, UUID clientId, String ip, String url, String userAgent, UserInfoType type, String language, String country, DateTime dateCreated, String email, String anonymousExperimentsLog, boolean isRecurringUser, Map<String, String> experimentOverrides, boolean robot, String host, BrowserVersion browserVersion) {
        this.experimentsLog = experimentsLog;
        this.userId = userId;
        this.type = type;
        this.clientId = clientId;
        this.ip = ip;
        this.url = url;
        this.userAgent = userAgent;
        this.language = language;
        this.country = country;
        this.dateCreated = dateCreated;
        this.email = email;
        this.anonymousExperimentsLog = anonymousExperimentsLog;
        this.isRecurringUser = isRecurringUser;
        this.experimentOverrides = experimentOverrides;
        this.isRobot = robot;
        this.host = host;
        this.browserVersion = browserVersion;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "anonymousExperimentsLog='" + anonymousExperimentsLog + '\'' +
                ", experimentsLog='" + experimentsLog + '\'' +
                '}';
    }

    public boolean isAnonymous() {
        return type.isAnonymous();
    }

    public TestGroup drawTestGroup(Experiment exp) {
        return type.drawTestGroup(exp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (isRecurringUser != userInfo.isRecurringUser) return false;
        if (isRobot != userInfo.isRobot) return false;
        if (anonymousExperimentsLog != null ? !anonymousExperimentsLog.equals(userInfo.anonymousExperimentsLog) : userInfo.anonymousExperimentsLog != null)
            return false;
        if (clientId != null ? !clientId.equals(userInfo.clientId) : userInfo.clientId != null) return false;
        if (country != null ? !country.equals(userInfo.country) : userInfo.country != null) return false;
        if (dateCreated != null ? !dateCreated.equals(userInfo.dateCreated) : userInfo.dateCreated != null)
            return false;
        if (email != null ? !email.equals(userInfo.email) : userInfo.email != null) return false;
        if (experimentOverrides != null ? !experimentOverrides.equals(userInfo.experimentOverrides) : userInfo.experimentOverrides != null)
            return false;
        if (experimentsLog != null ? !experimentsLog.equals(userInfo.experimentsLog) : userInfo.experimentsLog != null)
            return false;
        if (ip != null ? !ip.equals(userInfo.ip) : userInfo.ip != null) return false;
        if (language != null ? !language.equals(userInfo.language) : userInfo.language != null) return false;
        if (url != null ? !url.equals(userInfo.url) : userInfo.url != null) return false;
        if (userAgent != null ? !userAgent.equals(userInfo.userAgent) : userInfo.userAgent != null) return false;
        if (getUserId() != null ? !getUserId().equals(userInfo.getUserId()) : userInfo.getUserId() != null)
            return false;
        if (host != null ? !host.equals(userInfo.host) : userInfo.host != null) return false;
        if (browserVersion != null ? !browserVersion.equals(userInfo.browserVersion) : userInfo.browserVersion != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = experimentsLog != null ? experimentsLog.hashCode() : 0;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (getUserId() != null ? getUserId().hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (anonymousExperimentsLog != null ? anonymousExperimentsLog.hashCode() : 0);
        result = 31 * result + (isRecurringUser ? 1 : 0);
        result = 31 * result + (isRobot ? 1 : 0);
        result = 31 * result + (experimentOverrides != null ? experimentOverrides.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (browserVersion != null ? browserVersion.hashCode() : 0);
        return result;
    }

    public UserInfo setExperiments(ExperimentsLog experiments) {
        return new UserInfo(experiments.serialized(), getUserId(), clientId, ip, url, userAgent, type, language, country, dateCreated, email, anonymousExperimentsLog, isRecurringUser, experimentOverrides, isRobot, host, browserVersion);
    }

    public UserInfo setAnonymousExperiments(ExperimentsLog experiments) {
        return new UserInfo(experimentsLog, getUserId(), clientId, ip, url, userAgent, type, language, country, dateCreated, email, experiments.serialized(), isRecurringUser, experimentOverrides, isRobot, host, browserVersion);
    }

    public String getStorageKey() {
        return type.getStorageKey();
    }

    public UserInfo removeExperimentsWhere(ExperimentsLog.Predicate expired) {
        ExperimentsLog activeExperiments = filterExperiments(expired, this.experimentsLog);
        return setExperiments(activeExperiments);
    }

    private ExperimentsLog filterExperiments(ExperimentsLog.Predicate expired, String serializedLog) {
        ExperimentsLog experimentsLog = ExperimentsLog.parse(serializedLog);
        return experimentsLog.removeWhere(expired);
    }

    public UserInfo removeAnonymousExperimentsWhere(ExperimentsLog.Predicate predicate) {
        ExperimentsLog activeExperiments = filterExperiments(predicate, this.anonymousExperimentsLog);
        return setAnonymousExperiments(activeExperiments);
    }

    public boolean overridesExperiment(String keyName) {
        return experimentOverrides.containsKey(keyName);
    }

    public String getOverridenExperimentValue(String keyName) {
        return experimentOverrides.get(keyName);
    }

    public void saveExperimentState(ExperimentStateStorage experimentStateStorage) {
        experimentStateStorage.storeExperimentsLog(ANONYMOUS_LOG_STORAGE_KEY, anonymousExperimentsLog);
        //TODO - add tests. also prob make userinfo decide this instead of if
        if (!isAnonymous()) {
            experimentStateStorage.storeExperimentsLog(getStorageKey(), experimentsLog);
        }
        if (!experimentOverrides.isEmpty())
            experimentStateStorage.storeExperimentsOverrides(experimentOverrides);
    }

    public boolean participatesInExperiment(int id) {
        return allExperiments().containsExperiment(id);
    }


    private ExperimentsLog allExperiments() {
        return ExperimentsLog.parse(experimentsLog).appendAll(ExperimentsLog.parse(anonymousExperimentsLog));
    }

    public int winningTestGroupID(int experimentId) {
        return allExperiments().winningTestGroupId(experimentId);
    }

    public UserInfo appendAnonymousExperiments(String anonymousLog) {
        ExperimentsLog localAnonLogToAdd = ExperimentsLog.parse(anonymousLog);
        ExperimentsLog existingAnonLog = ExperimentsLog.parse(anonymousExperimentsLog);
        return setAnonymousExperiments(existingAnonLog.appendAll(localAnonLogToAdd));
    }

    public UserInfo appendUserExperiments(String userLog) {
        ExperimentsLog localUserLogToAdd = ExperimentsLog.parse(userLog);
        ExperimentsLog existingUserLog = ExperimentsLog.parse(experimentsLog);
        return setExperiments(existingUserLog.appendAll(localUserLogToAdd));
    }


    public UUID getUserId() {
        return userId;
    }
}
