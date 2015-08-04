package com.wixpress.petri.laboratory;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 10/6/14
* Time: 4:01 PM
* This class is mutable and therefore a new instance should be generated for every HttpRequest.
*
*/
public class RequestScopedUserInfoStorage implements UserInfoStorage {
    private final UserInfo originalInstance;
    private UserInfo storedInstance = null;


    public RequestScopedUserInfoStorage(UserInfoExtractor extractor) {
        this.originalInstance = extractor.extract();
    }

    @Override
    public void write(UserInfo info) {
        storedInstance = info;
    }

    @Override
    public UserInfo read() {
        final UserInfo userInfo = storedInstance;
        return userInfo == null ? originalInstance : userInfo;
    }

    public UserInfo readOriginal() {
        return originalInstance;
    }
}
