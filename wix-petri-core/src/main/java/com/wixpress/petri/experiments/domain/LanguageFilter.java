package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author: talyag
 * @since: 11/26/13
 */
public class LanguageFilter implements Filter {

    private List<String> languages;

    public List<String> getLanguages() {
        return languages;
    }

    @JsonCreator
    public LanguageFilter(@JsonProperty("languages") List<String> languages) {
        this.languages = languages;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return languages.contains(eligibilityCriteria.getLanguage());
    }

    @Override
    public String toString() {
        return "LanguageFilter{" +
                "languages=" + languages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LanguageFilter languageFilter = (LanguageFilter) o;

        if (languages != null ? !languages.equals(languageFilter.languages) : languageFilter.languages != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return languages != null ? languages.hashCode() : 0;
    }
}
