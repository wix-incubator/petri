package filters;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;
import com.wixpress.petri.experiments.domain.FilterTypeName;

/**
 * @author talyag
 * @since 9/9/14
 */
@FilterTypeName("additionalFilter")
public class AdditionalFilter implements Filter {

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
