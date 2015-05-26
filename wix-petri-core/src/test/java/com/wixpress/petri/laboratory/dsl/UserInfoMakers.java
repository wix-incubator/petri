package com.wixpress.petri.laboratory.dsl;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Maker;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoType;
import com.wixpress.petri.laboratory.UserInfoTypeFactory;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * @author sagyr
 * @since 8/6/13
 */
public class UserInfoMakers {

    public static final Property<UserInfo, String> experimentsLog = newProperty();
    public static final Property<UserInfo, HashMap<UUID, String>> otherUserExperimentsLog = newProperty();
    public static final Property<UserInfo, UUID> userId = newProperty();
    public static final Property<UserInfo, UUID> clientId = newProperty();
    public static final Property<UserInfo, String> ip = newProperty();
    public static final Property<UserInfo, String> url = newProperty();
    public static final Property<UserInfo, String> userAgent = newProperty();
    public static final Property<UserInfo, String> language = newProperty();
    public static final Property<UserInfo, String> country = newProperty();
    public static final Property<UserInfo, DateTime> dateCreated = newProperty();
    public static final Property<UserInfo, String> email = newProperty();
    public static final Property<UserInfo, String> anonymousExperimentsLog = newProperty();
    public static final Property<UserInfo, Boolean> recurringUser = newProperty();
    public static final Property<UserInfo, Boolean> robot = newProperty();
    public static final Property<UserInfo, Map<String, String>> experimentOverrides = newProperty();
    public static final Property<UserInfo, UserInfoType> userInfoType = newProperty();
    public static final Property<UserInfo, String> host = newProperty();

    public static final Instantiator<UserInfo> UserInfo = new Instantiator<UserInfo>() {

        @Override
        public UserInfo instantiate(PropertyLookup<UserInfo> lookup) {

            final String experimentsLog = lookup.valueOf(UserInfoMakers.experimentsLog, "");
            final UUID userId = lookup.valueOf(UserInfoMakers.userId, UUID.randomUUID());
            final UUID clientId = lookup.valueOf(UserInfoMakers.clientId, UUID.randomUUID());
            final String ip = lookup.valueOf(UserInfoMakers.ip, "");
            final String url = lookup.valueOf(UserInfoMakers.url, "");
            final String userAgent = lookup.valueOf(UserInfoMakers.userAgent, "");
            final UserInfoType userInfoType = UserInfoTypeFactory.make(userId);
            final String language = lookup.valueOf(UserInfoMakers.language, "");
            final String country = lookup.valueOf(UserInfoMakers.country, "");
            final DateTime dateCreated = lookup.valueOf(UserInfoMakers.dateCreated, new DateTime());
            final String email = lookup.valueOf(UserInfoMakers.email, "");
            final String anonymousExperimentsLog = lookup.valueOf(UserInfoMakers.anonymousExperimentsLog, "");
            final Boolean recurringUser = lookup.valueOf(UserInfoMakers.recurringUser, true);
            final Map<String, String> experimentOverrides = lookup.valueOf(UserInfoMakers.experimentOverrides, new HashMap<String, String>());
            final Boolean robot = lookup.valueOf(UserInfoMakers.robot, false);
            final String host = lookup.valueOf(UserInfoMakers.host, "");
            final HashMap<UUID, String> otherUserExperimentsLog = lookup.valueOf(UserInfoMakers.otherUserExperimentsLog, new HashMap<UUID, String>());

            return new UserInfo(experimentsLog, userId, clientId,
                    ip, url, userAgent,
                    userInfoType, language,
                    country,
                    dateCreated, email,
                    anonymousExperimentsLog, recurringUser,
                    experimentOverrides, robot, host, otherUserExperimentsLog);
        }
    };


    public static Maker<com.wixpress.petri.laboratory.UserInfo> AnonymousUserInfo = a(UserInfo).but(
            with(userInfoType, UserInfoTypeFactory.make(null)),
            with(userId, (UUID) null),
            with(clientId, (UUID) null),
            with(recurringUser, false));

}
