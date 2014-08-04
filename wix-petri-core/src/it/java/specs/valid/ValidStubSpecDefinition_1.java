package specs.valid;

import com.wixpress.petri.experiments.domain.ScopeDefinition;
import com.wixpress.petri.petri.SpecDefinition;

import java.util.List;

import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionForAllUserTypes;
import static java.util.Arrays.asList;

/**
 * @author sagyr
 * @since 10/6/13
 */
public class ValidStubSpecDefinition_1 extends SpecDefinition {

    public static List<String> testGroups = asList("1", "2");
    public static ScopeDefinition[] scopeDefinitions =
            {aScopeDefinitionForAllUserTypes("scope1"),
                    aScopeDefinitionForAllUserTypes("scope2")};

    @Override
    protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
        return builder.withTestGroups(testGroups).
                withScopes(scopeDefinitions);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return getClass().equals(obj.getClass());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
