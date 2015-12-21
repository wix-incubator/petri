package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.joda.time.DateTime;
import scala.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.wixpress.petri.laboratory.UserInfoType.ANONYMOUS_LOG_STORAGE_KEY;

public class UserInfo implements ConductionStrategy {

    private static final DateTime BEGINNING_OF_TIME = new DateTime(0);
    private static final HashMap<UUID, String> emptyOtherUsersExperimentsLogs = new HashMap<UUID, String>();

    public static UserInfo userInfoFromNullRequest(String host) {
        return new UserInfo("", (UUID) null, null, "", "", "",
                new NullUserInfoType(), "", "", BEGINNING_OF_TIME, false, "", false, new HashMap<String, String>(), false, host, emptyOtherUsersExperimentsLogs, emptyOtherUsersExperimentsLogs, false, "");
    }

    private static UserInfo userInfoWithNoExperimentsLogs() {
        return new UserInfo("", (UUID) null, null, "", "", "",
                new NullUserInfoType(), "", "", BEGINNING_OF_TIME, false, "", false, new HashMap<String, String>(), false, "n/a", emptyOtherUsersExperimentsLogs, emptyOtherUsersExperimentsLogs, false, "");
    }

    public final String experimentsLog;
    public final String anonymousExperimentsLog;
    public final Map<UUID, String> otherUsersExperimentsLogs;
    public final Map<UUID, String> potentialOtherUserExperimentsLogFromCookies;
    private final UserInfoType type;
    public final UUID clientId;
    public final String ip;
    public final String url;
    public final String userAgent;
    private final UUID userId;
    public final String language;
    public final String country;
    public final DateTime dateCreated;
    public final boolean companyEmployee;
    public final boolean isRecurringUser;
    public boolean registeredUserExists;
    public final Map<String, String> experimentOverrides;
    public final boolean isRobot;
    public final String host;
    public final String globalSessionId;

    //keeping a simple version for clients that are no interested in parsing/passing values of users other than the user in session
    public UserInfo(String experimentsLog, UUID userId, UUID clientId, String ip, String url, String userAgent, UserInfoType userInfoType, String language, String country, DateTime userCreationDate, boolean companyEmployee, String anonymousExperimentsLog, boolean isRecurring, Map<String, String> experimentOverrides, boolean isRobot, String host, boolean registeredUserExists) {
        this(experimentsLog, userId, clientId, ip, url, userAgent, userInfoType,
                language, country, userCreationDate, companyEmployee, anonymousExperimentsLog, isRecurring,
                experimentOverrides, isRobot, host, new HashMap<UUID, String>(), new HashMap<UUID, String>(), registeredUserExists, "");
    }

    public UserInfo(String experimentsLog, UUID userId, UUID clientId, String ip, String url, String userAgent, UserInfoType type, String language, String country, DateTime dateCreated, boolean companyEmployee, String anonymousExperimentsLog, boolean isRecurringUser, Map<String, String> experimentOverrides, boolean robot, String host, Map<UUID, String> otherUsersExperimentsLogs, Map<UUID, String> potentialOtherUserExperimentsLogFromCookies, boolean registeredUserExists, String globalSessionId) {
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
        this.companyEmployee = companyEmployee;
        this.anonymousExperimentsLog = anonymousExperimentsLog;
        this.isRecurringUser = isRecurringUser;
        this.experimentOverrides = experimentOverrides;
        this.isRobot = robot;
        this.host = host;
        this.otherUsersExperimentsLogs = otherUsersExperimentsLogs;
        this.potentialOtherUserExperimentsLogFromCookies = potentialOtherUserExperimentsLogFromCookies;
        this.registeredUserExists = registeredUserExists;
        this.globalSessionId = globalSessionId;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "anonymousExperimentsLog='" + anonymousExperimentsLog + '\'' +
                ", experimentsLog='" + experimentsLog + '\'' +
                ", otherUserExperimentsLog='" + otherUsersExperimentsLogs + '\'' +
                ", potentialOtherUserExperimentsLogFromCookies='" + potentialOtherUserExperimentsLogFromCookies + '\'' +
                '}';
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isAnonymous() {
        return type.isAnonymous();
    }

    @Override
    public Option<UUID> persistentKernel() {
        return type.persistentKernel();
    }


    @Override
    public boolean shouldPersist() {
        return type.shouldPersist();
    }

    @Override
    public Option<UUID> getUserIdRepresentedForFlow(Option<UUID> userInSession) {
        return type.getUserIdRepresentedForFlow(userInSession);
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
        if (companyEmployee != userInfo.companyEmployee) return false;
        if (experimentOverrides != null ? !experimentOverrides.equals(userInfo.experimentOverrides) : userInfo.experimentOverrides != null)
            return false;
        if (experimentsLog != null ? !experimentsLog.equals(userInfo.experimentsLog) : userInfo.experimentsLog != null)
            return false;
        if (host != null ? !host.equals(userInfo.host) : userInfo.host != null) return false;
        if (ip != null ? !ip.equals(userInfo.ip) : userInfo.ip != null) return false;
        if (language != null ? !language.equals(userInfo.language) : userInfo.language != null) return false;
        if (otherUsersExperimentsLogs != null ? !otherUsersExperimentsLogs.equals(userInfo.otherUsersExperimentsLogs) : userInfo.otherUsersExperimentsLogs != null)
            return false;
        if (url != null ? !url.equals(userInfo.url) : userInfo.url != null) return false;
        if (userAgent != null ? !userAgent.equals(userInfo.userAgent) : userInfo.userAgent != null) return false;
        if (userId != null ? !userId.equals(userInfo.userId) : userInfo.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = experimentsLog != null ? experimentsLog.hashCode() : 0;
        result = 31 * result + (anonymousExperimentsLog != null ? anonymousExperimentsLog.hashCode() : 0);
        result = 31 * result + (otherUsersExperimentsLogs != null ? otherUsersExperimentsLogs.hashCode() : 0);
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
        result = 31 * result + (companyEmployee ? 1 : 0);
        result = 31 * result + (isRecurringUser ? 1 : 0);
        result = 31 * result + (experimentOverrides != null ? experimentOverrides.hashCode() : 0);
        result = 31 * result + (isRobot ? 1 : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        return result;
    }

    public UserInfo setExperiments(ExperimentsLog experiments) {
        return new UserInfo(experiments.serialized(), userId, clientId, ip, url, userAgent, type, language, country, dateCreated,
                companyEmployee, anonymousExperimentsLog, isRecurringUser, experimentOverrides, isRobot, host, otherUsersExperimentsLogs,
                potentialOtherUserExperimentsLogFromCookies, registeredUserExists, globalSessionId);
    }

    private UserInfo setOtherUserExperiments(UUID uuid, ExperimentsLog otherUserExperimentsLog) {
        HashMap<UUID, String> otherUserExperimentsLogAppended = new HashMap<>(otherUsersExperimentsLogs);
        otherUserExperimentsLogAppended.put(uuid, otherUserExperimentsLog.serialized());
        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, type, language, country, dateCreated,
                companyEmployee, anonymousExperimentsLog, isRecurringUser, experimentOverrides, isRobot, host, otherUserExperimentsLogAppended,
                potentialOtherUserExperimentsLogFromCookies, registeredUserExists, globalSessionId);
    }

    public UserInfo setAnonymousExperiments(ExperimentsLog experiments) {
        return new UserInfo(experimentsLog, userId, clientId, ip, url, userAgent, type, language, country, dateCreated,
                companyEmployee, experiments.serialized(), isRecurringUser, experimentOverrides, isRobot, host, otherUsersExperimentsLogs,
                potentialOtherUserExperimentsLogFromCookies, registeredUserExists, globalSessionId);
    }

    //note - not dealing with removing otherUsers experiments as these are persisted as session cookies so no need
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
        saveExperimentState(experimentStateStorage, userInfoWithNoExperimentsLogs());
    }

    public void saveExperimentState(ExperimentStateStorage experimentStateStorage, UserInfo originalUserInfo) {
        if (!anonymousExperimentsLog.equals(originalUserInfo.anonymousExperimentsLog)){
            experimentStateStorage.storeAnonymousExperimentsLog(ANONYMOUS_LOG_STORAGE_KEY, anonymousExperimentsLog);
        }

        if (!experimentsLog.equals(originalUserInfo.experimentsLog)){
            experimentStateStorage.storeUserExperimentsLog(userId, userId, experimentsLog);
        }

        if (!otherUsersExperimentsLogs.equals(originalUserInfo.otherUsersExperimentsLogs)){
            for (UUID otherUser : otherUsersExperimentsLogs.keySet())
            experimentStateStorage.storeUserExperimentsLog(userId, otherUser, otherUsersExperimentsLogs.get(otherUser));
        }

        if (!experimentOverrides.isEmpty())
            experimentStateStorage.storeExperimentsOverrides(experimentOverrides);

    }

    private ExperimentsLog  allExperiments(UUID uid) {
        if (uid == null || uid.equals(userId))
            return ExperimentsLog.parse(experimentsLog).appendAll(ExperimentsLog.parse(anonymousExperimentsLog));
        else
            return ExperimentsLog.parse(otherUsersExperimentsLogs.get(uid)).appendAll(ExperimentsLog.parse(anonymousExperimentsLog));
    }

    public Map<String, String> getWinningExperiments(UUID uid){
         return  allExperiments(uid).getWinningTestGroups();
    }

    public UserInfo addRelevantUserLogToContext(UUID uid) {
        String relevantLogsFromExistingCookies = potentialOtherUserExperimentsLogFromCookies.get(uid);
        if (relevantLogsFromExistingCookies != null && !relevantLogsFromExistingCookies.isEmpty())
            return appendOtherUserExperiments(relevantLogsFromExistingCookies, uid);
        else
            return this;
    }

    public UserInfo appendAnonymousExperiments(String anonymousLog) {
        ExperimentsLog localAnonLogToAdd = ExperimentsLog.parse(anonymousLog);
        ExperimentsLog existingAnonLog = ExperimentsLog.parse(anonymousExperimentsLog);
        return setAnonymousExperiments(existingAnonLog.appendAll(localAnonLogToAdd));
    }

    public UserInfo appendUserExperiments(String logToAppend, Option<UUID> uuidOption) {
        if (uuidOption.isEmpty())
            return this;

        UUID uuid = uuidOption.get();
        if (uuid.equals(userId))
            return appendTheUsersExperiments(logToAppend);
        else {
            return appendOtherUserExperiments(logToAppend, uuid);
        }
    }

    private UserInfo appendTheUsersExperiments(String userLog) {
        ExperimentsLog localUserLogToAdd = ExperimentsLog.parse(userLog);
        ExperimentsLog existingUserLog = ExperimentsLog.parse(experimentsLog);
        return setExperiments(existingUserLog.appendAll(localUserLogToAdd));
    }

    private UserInfo appendOtherUserExperiments(String userLog, UUID uuid) {
        ExperimentsLog localUserLogToAdd = ExperimentsLog.parse(userLog);
        ExperimentsLog existingUserLog = ExperimentsLog.parse(otherUsersExperimentsLogs.get(uuid));
        return setOtherUserExperiments(uuid, existingUserLog.appendAll(localUserLogToAdd));
    }


}
