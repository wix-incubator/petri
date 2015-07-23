package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;
import org.joda.time.DateTime;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ExperimentPredicates {
    public static class IsActivePredicate implements Predicate<Experiment> {

        private DateTime now;

        private IsActivePredicate(DateTime now) {
            this.now = now;
        }

        @Override
        public boolean apply(Experiment input) {
            return input.isActiveAt(now);
        }

        public static IsActivePredicate isActiveAt(DateTime now) {
            return new IsActivePredicate(now);
        }

        public static IsActivePredicate isActiveNow() {
            return isActiveAt(new DateTime());
        }
    }

    public static class IsInScope implements Predicate<Experiment> {
        private final String scope;

        public IsInScope(String scope) {
            this.scope = scope;
        }

        @Override
        public boolean apply(Experiment input) {
            return input.getScope().equals(scope);
        }

        public static IsInScope isInScope(String scope) {
            return new IsInScope(scope);
        }
    }

    public static class HasID implements Predicate<Experiment> {
        private final int id;

        public HasID(int id) {
            this.id = id;
        }

        @Override
        public boolean apply(Experiment input) {
            return input.getId() == id;
        }

        public static HasID hasID(int id) {
            return new HasID(id);
        }
    }

    public static class HasOriginalId implements Predicate<Experiment> {
        private final int id;

        public HasOriginalId(int id) {
            this.id = id;
        }

        @Override
        public boolean apply(Experiment input) {
            return input.getOriginalId() == id;
        }

        public static HasOriginalId hasOriginalId(int id) {
            return new HasOriginalId(id);
        }
    }

    public static class HasKey implements Predicate<Experiment> {

        private final String key;

        public HasKey(String key) {
            this.key = key;
        }

        @Override
        public boolean apply(Experiment input) {
            return input.getKey().equalsIgnoreCase(key);
        }

        public static HasKey hasKey(String key) {
            return new HasKey(key);
        }
    }

    public static class IsNotTerminated implements Predicate<Experiment> {

        @Override
        public boolean apply(Experiment input) {
            return !input.isTerminated();
        }

        public static IsNotTerminated isNotTerminated() {
            return new IsNotTerminated();

        }
    }

    public static class IsNotPaused implements Predicate<Experiment> {

        @Override
        public boolean apply(Experiment input) {
            return !input.isPaused();
        }

        public static IsNotPaused isNotPaused() {
            return new IsNotPaused();

        }
    }

    public static class SpecHasKey implements Predicate<ExperimentSpec> {

        private final String key;

        public SpecHasKey(String key) {
            this.key = key;
        }

        @Override
        public boolean apply(ExperimentSpec input) {
            return input.getKey().equalsIgnoreCase(key);
        }

        public static SpecHasKey specHasKey(String key) {
            return new SpecHasKey(key);
        }
    }
}
