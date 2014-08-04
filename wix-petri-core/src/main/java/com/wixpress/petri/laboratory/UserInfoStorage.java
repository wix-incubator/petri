package com.wixpress.petri.laboratory;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 11/4/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserInfoStorage {
    void write(UserInfo info);

    UserInfo read();
}
