package dynamic.filters;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;
import com.wixpress.petri.experiments.domain.FilterTypeName;

/**
 * Created by talyas on 2/1/15.
 */
@FilterTypeName("SomeCustomFilter")
public class SomeCustomFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        throw new UnsupportedOperationException("this filter is only used for testing type loading");
    }
}
