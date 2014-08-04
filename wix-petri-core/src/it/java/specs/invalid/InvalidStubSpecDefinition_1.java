package specs.invalid;

import com.wixpress.petri.petri.SpecDefinition;

import static java.util.Arrays.asList;

/**
 * @author sagyr
 * @since 10/6/13
 */

/**
 * This is an example of an invalid spec definition.
 * It is invalid because it is not a public class
 */
class InvalidStubSpecDefinition_1 extends SpecDefinition {

    @Override
    protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
        return builder.withTestGroups(asList("1", "2"));
    }
}
