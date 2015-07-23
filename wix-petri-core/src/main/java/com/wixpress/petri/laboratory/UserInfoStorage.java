package com.wixpress.petri.laboratory;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface UserInfoStorage {
    void write(UserInfo info);

    UserInfo read();
}
