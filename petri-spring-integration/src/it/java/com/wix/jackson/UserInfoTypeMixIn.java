package com.wix.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wixpress.petri.laboratory.NullUserInfoType;
import com.wixpress.petri.laboratory.RegisteredUserInfoType;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 8:55 AM
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonSubTypes({@JsonSubTypes.Type(value = NullUserInfoType.class, name = "NullUserInfoType"),
               @JsonSubTypes.Type(value = RegisteredUserInfoType.class, name = "RegisteredUserInfoType")
})
public interface UserInfoTypeMixIn {
}
