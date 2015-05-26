package com.wixpress.petri.laboratory;

public interface UserInfoType extends ConductionStrategy {

    public static final String ANONYMOUS_LOG_STORAGE_KEY = "_wixAB3";

    public boolean isAnonymous();

}