package com.wixpress.petri.laboratory;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author sagyr
 * @since 8/7/13
 */
public class ExperimentsLogTest {

    @Test
    public void emptyLogContainsNothing() {
        assertFalse("Should not contain anything", ExperimentsLog.parse("").containsExperiment(1234));
    }

    @Test
    public void logWithSingleEntryContainsTheEntry() {
        assertTrue("Should contain the experiment", ExperimentsLog.parse("1#3").containsExperiment(1));
    }

    @Test
    public void logWithTwoEntriesContainsTheEntry() {
        assertTrue("2#5 should contain experiment 2", ExperimentsLog.parse("2#5|1#4").containsExperiment(2));
        assertTrue("2#5|1#4 should contain experiment 1", ExperimentsLog.parse("2#5|1#4").containsExperiment(1));
    }

    @Test(expected = MalformedExperimentsLogException.class)
    public void throwsOnMalformedInput() {
        ExperimentsLog.parse("12");
    }

    @Test
    public void canSerializeToString() {
        assertThat(ExperimentsLog.parse("1#2").serialized(), is("1#2"));
    }

    @Test
    public void preservesSerializationOrder() {
        assertThat(ExperimentsLog.parse("1#2|3#4").serialized(), is("1#2|3#4"));
    }

    @Test
    public void preservesSerializationOrderWhenAppending() {
        assertThat(ExperimentsLog.parse("1#2|3#4").appendExperiment(4, 5).serialized(), is("1#2|3#4|4#5"));
    }

    @Test
    public void whenRemovingSingleExperimentLogIsEmpty() {
        assertThat(ExperimentsLog.parse("12#13").removeExperiment(12), is(ExperimentsLog.parse("")));
    }

    @Test
    public void canRemoveAnyExperiment() {
        assertThat(ExperimentsLog.parse("1#3|2#4|3#5").removeExperiment(2), is(ExperimentsLog.parse("1#3|3#5")));
    }

    @Test
    public void canRemoveExperimentsByPredicate() {
        assertThat(ExperimentsLog.parse("1#3|2#2|3#3").removeWhere(new ExperimentsLog.Predicate() {
            @Override
            public boolean matches(int experimentId) {
                return experimentId == 3;
            }
        }), is(ExperimentsLog.parse("1#3|2#2")));
    }

    @Test
    public void logsCanBeAppended() {
        assertThat(ExperimentsLog.parse("1#2").appendAll(ExperimentsLog.parse("3#4")).serialized(), is("1#2|3#4"));
    }

}
