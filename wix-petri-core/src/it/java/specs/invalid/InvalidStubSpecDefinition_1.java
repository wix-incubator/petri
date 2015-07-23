package specs.invalid;

import com.wixpress.petri.petri.SpecDefinition;

import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
class InvalidStubSpecDefinition_1 extends SpecDefinition {

    @Override
    protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
        return builder.withTestGroups(asList("1", "2"));
    }
}
