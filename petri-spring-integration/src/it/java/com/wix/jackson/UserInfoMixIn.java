package com.wix.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.wixpress.petri.laboratory.UserInfoType;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

/**
 * User: Dalias
 * Date: 8/13/14
 * Time: 4:47 PM
 */
public abstract class UserInfoMixIn {


    @JsonCreator
    public UserInfoMixIn(@JsonProperty("experimentsLog") String experimentsLog,
                         @JsonProperty("userId") UUID userId,
                         @JsonProperty("clientId") UUID clientId,
                         @JsonProperty("ip") String ip,
                         @JsonProperty("url") String url,
                         @JsonProperty("userAgent") String userAgent,
                         @JsonProperty("userInfoType") UserInfoType type,
                         @JsonProperty("language") String language,
                         @JsonProperty("country") String country,
                         @JsonProperty("dateCreated") DateTime dateCreated,
                         @JsonProperty("email") String email,
                         @JsonProperty("anonymous") String anonymousExperimentsLog,
                         @JsonProperty("isRecurringUser") boolean isRecurringUser,
                         @JsonProperty("experimentOverrides") Map<String, String> experimentOverrides,
                         @JsonProperty("robot") boolean robot,
                         @JsonProperty("host") String host) {
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
    }

    @JsonProperty("experimentsLog")
    String experimentsLog;
    @JsonProperty("userInfoType")
    UserInfoType type;
    @JsonProperty("clientId")
    UUID clientId;
    @JsonProperty("ip")
    String ip;
    @JsonProperty("url")
    String url;
    @JsonProperty("userAgent")
    String userAgent;
    @JsonProperty("userId")
    UUID userId;
    @JsonProperty("language")
    String language;
    @JsonProperty("country")
    String country;
    @JsonProperty("dateCreated")
    DateTime dateCreated;
    @JsonProperty("email")
    String email;
    @JsonProperty("anonymousExperimentsLog")
    String anonymousExperimentsLog;
    @JsonProperty("isRecurringUser")
    boolean isRecurringUser;
    @JsonProperty("experimentOverrides")
    Map<String, String> experimentOverrides;
    @JsonProperty("isRobot")
    boolean isRobot;
    @JsonProperty("host")
    String host;


    @JsonIgnore
    public String getStorageKey() {
        return type.getStorageKey();
    }

    @JsonIgnore
    public boolean isAnonymous() {
        return type.isAnonymous();
    }


}
