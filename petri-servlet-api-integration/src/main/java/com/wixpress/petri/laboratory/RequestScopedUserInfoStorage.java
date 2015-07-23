package com.wixpress.petri.laboratory;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
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
