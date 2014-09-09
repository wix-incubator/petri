package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.EligibilityField;
import com.wixpress.petri.laboratory.UserInfo;
import org.joda.time.DateTime;

import java.util.UUID;

public class FilterEligibility {
    private final UserInfo user;
    private final EligibilityFields fields;
    private final DateTime experimentStartDate;

    public FilterEligibility(UserInfo userInfo, EligibilityFields fields, DateTime experimentStartDate) {
        this.user = userInfo;
        this.fields = fields;
        this.experimentStartDate = experimentStartDate;
    }

    public DateTime getExperimentStartDate() {
        return experimentStartDate;
    }

    public <T extends EligibilityField> T getField(Class<T> fieldClass) {
        return fields.getField(fieldClass);
    }

    public UUID getUserId() {
        return user.getUserId();
    }

    public String getLanguage() {
        return user.language;
    }

    public boolean isAnonymous() {
        return user.isAnonymous();
    }

    public boolean isRecurringUser() {
        return user.isRecurringUser;
    }

    public String getCountry() {
        return user.country;
    }

    public String getHost() {
        return user.host;
    }

    public String getEmail() {
        return user.email;
    }

    public DateTime getUserCreationDate() {
        return user.dateCreated;
    }


}
