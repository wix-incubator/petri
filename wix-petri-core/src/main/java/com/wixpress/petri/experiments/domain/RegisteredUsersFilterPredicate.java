package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 3/6/14
* Time: 2:19 PM
* To change this template use File | Settings | File Templates.
*/
class RegisteredUsersFilterPredicate implements Predicate<Filter> {
    RegisteredUsersFilterPredicate() {
    }

    public static RegisteredUsersFilterPredicate registeredUsersFilter() {
        return new RegisteredUsersFilterPredicate();
    }

    @Override
    public boolean apply(@Nullable Filter input) {
        return input instanceof RegisteredUsersFilter;
    }


}
