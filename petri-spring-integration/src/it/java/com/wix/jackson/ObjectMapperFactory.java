package com.wix.jackson;

import com.wixpress.petri.laboratory.NullUserInfoType;
import com.wixpress.petri.laboratory.RegisteredUserInfoType;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoType;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 2:09 PM
 */
public class ObjectMapperFactory {


    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.getSerializationConfig().addMixInAnnotations(UserInfoType.class, UserInfoTypeMixIn.class);
        objectMapper.getSerializationConfig().addMixInAnnotations(RegisteredUserInfoType.class, RegisteredUserInfoTypeMixIn.class);
        objectMapper.getSerializationConfig().addMixInAnnotations(NullUserInfoType.class, NullUserInfoTypeMixIn.class);
        objectMapper.getSerializationConfig().addMixInAnnotations(UserInfo.class, UserInfoMixIn.class);

        objectMapper.getDeserializationConfig().addMixInAnnotations(UserInfoType.class, UserInfoTypeMixIn.class);
        objectMapper.getDeserializationConfig().addMixInAnnotations(RegisteredUserInfoType.class, RegisteredUserInfoTypeMixIn.class);
        objectMapper.getDeserializationConfig().addMixInAnnotations(NullUserInfoType.class, NullUserInfoTypeMixIn.class);
        objectMapper.getDeserializationConfig().addMixInAnnotations(UserInfo.class, UserInfoMixIn.class);

        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        return objectMapper;
    }
}
