package com.wixpress.petri.laboratory;

import com.google.common.base.Joiner;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExperimentsLog {

    private final Map<String, String> experimentsResults;

    public ExperimentsLog(Map<String, String> experimentResults) {
        this.experimentsResults = experimentResults;
    }

    public static ExperimentsLog empty() {
        return ExperimentsLog.parse("");
    }

    public static ExperimentsLog parse(String runningExperiments) {
        try {
            Map<String, String> experiments = new LinkedHashMap<>();
            if (runningExperiments.isEmpty())
                return new ExperimentsLog(experiments);

            for (String experiment : runningExperiments.split("\\|")) {
                String[] parsedExperiment = experiment.split("#");

                String key = parsedExperiment[0];
                String value = parsedExperiment[1];
                validateExperiment(key, value);

                experiments.put(key, value);
            }
            return new ExperimentsLog(experiments);
        } catch (Throwable t) {
            throw new MalformedExperimentsLogException(runningExperiments);
        }
    }

    private static void validateExperiment(String key, String value) {
        validateInteger(key);
        validateInteger(value);
    }

    private static void validateInteger(String i) {
        Integer.valueOf(i);
    }

    public String serialized() {
        return Joiner.on("|").withKeyValueSeparator("#").join(experimentsResults);
    }

    public boolean containsExperiment(int experimentId) {
        return experimentsResults.containsKey("" + experimentId);
    }

    public String winningGroupId(int experimentId) {
        return experimentsResults.get(String.valueOf(experimentId));
    }

    public ExperimentsLog appendExperiment(int experimentId, int testGroupId) {
        Map<String, String> updatedExperiments = new LinkedHashMap<String, String>(experimentsResults);
        updatedExperiments.put("" + experimentId, "" + testGroupId);
        return new ExperimentsLog(updatedExperiments);
    }

    public ExperimentsLog removeExperiment(int experimentId) {
        Map<String, String> updatedExperiments = new LinkedHashMap<String, String>(experimentsResults);
        updatedExperiments.remove("" + experimentId);
        return new ExperimentsLog(updatedExperiments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentsLog that = (ExperimentsLog) o;

        if (!experimentsResults.equals(that.experimentsResults)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + experimentsResults.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExperimentsLog{" +
                ", experimentsResults=" + experimentsResults +
                '}';
    }

    public ExperimentsLog removeWhere(Predicate predicate) {
        LinkedHashMap<String, String> filteredResults = new LinkedHashMap<String, String>(experimentsResults);
        for (Map.Entry<String, String> s : experimentsResults.entrySet()) {
            if (predicate.matches(Integer.valueOf(s.getKey())))
                filteredResults.remove(s.getKey());
        }
        return new ExperimentsLog(filteredResults);
    }

    public int winningTestGroupId(int experimentId) {
        final String experimentValueAsString = winningGroupId(experimentId);
        return Integer.parseInt(experimentValueAsString);
    }

    public ExperimentsLog appendAll(ExperimentsLog other) {
        Map<String, String> combinedExperiments = new LinkedHashMap<>(experimentsResults);
        combinedExperiments.putAll(other.experimentsResults);
        return new ExperimentsLog(combinedExperiments);
    }

    public interface Predicate {
        boolean matches(int experimentId);
    }


}
