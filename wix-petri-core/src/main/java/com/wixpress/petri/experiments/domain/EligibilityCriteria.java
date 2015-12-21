package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.ConductionStrategy;
import com.wixpress.petri.laboratory.EligibilityCriteriaTypes;
import com.wixpress.petri.laboratory.EligibilityCriterion;
import com.wixpress.petri.laboratory.UserInfo;
import org.joda.time.DateTime;
import scala.Option;

import java.util.UUID;

import static com.wixpress.petri.laboratory.EligibilityCriteriaTypes.LanguageCriterion;
import static com.wixpress.petri.laboratory.EligibilityCriteriaTypes.UserCreationDateCriterion;

public class EligibilityCriteria {
    private final AdditionalEligibilityCriteria additionalCriteria;

    private final DateTime experimentStartDate;

    private final DateTime userCreationDate;
    private final boolean companyEmployee;
    private final String host;
    private final String country;
    private final boolean isRecurringUser;
    private final boolean isAnonymous;
    private final UUID userId;
    private final String language;
    private final String userAgent;
    private final boolean registeredUserExists;

    public EligibilityCriteria(UserInfo userInfo, AdditionalEligibilityCriteria additionalCriteria, DateTime experimentStartDate) {
        this(userInfo, userInfo, additionalCriteria, experimentStartDate);
    }

    public EligibilityCriteria(UserInfo userInfo, ConductionStrategy conductionStrategy, AdditionalEligibilityCriteria additionalCriteria, DateTime experimentStartDate) {
        this.experimentStartDate = experimentStartDate;
        this.additionalCriteria = additionalCriteria;

        Option<UUID> userInSession = scala.Option.apply(userInfo.getUserId());
        Option<UUID> userRepresentedForFlow = conductionStrategy.getUserIdRepresentedForFlow(userInSession);
        this.userId = userRepresentedForFlow.isDefined() ? userRepresentedForFlow.get() : null;
        this.isAnonymous = userRepresentedForFlow.isEmpty();

        this.userCreationDate = overrideCriterionOrElse(UserCreationDateCriterion.class, userInfo.dateCreated);
        this.language = overrideCriterionOrElse(LanguageCriterion.class, userInfo.language);
        this.country = overrideCriterionOrElse(EligibilityCriteriaTypes.CountryCriterion.class, userInfo.country);

        this.isRecurringUser = userInfo.isRecurringUser;
        this.registeredUserExists = userInfo.registeredUserExists;
        this.host = userInfo.host;
        this.companyEmployee = userInfo.companyEmployee;
        this.userAgent = userInfo.userAgent;
    }

    public <V, T extends EligibilityCriterion<V>> V getAdditionalCriterion(Class<T> criterionClass) {
        return overrideCriterionOrElse(criterionClass, null);
    }

    private <V, T extends EligibilityCriterion<V>> V overrideCriterionOrElse(Class<T> criterionClass, V defaultValue) {
        T criterion = additionalCriteria.getCriterion(criterionClass);
        if (criterion != null)
            return criterion.getValue();
        return defaultValue;
    }


    public String getLanguage() {
        return language;
    }

    public DateTime getExperimentStartDate() {
        return experimentStartDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public boolean isRecurringUser() {
        return isRecurringUser;
    }

    public String getCountry() {
        return country;
    }

    public String getHost() {
        return host;
    }

    public boolean isCompanyEmployee() {
        return companyEmployee;
    }

    public DateTime getUserCreationDate() {
        return userCreationDate;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isRegisteredUserExists() {
        return registeredUserExists;
    }
}
