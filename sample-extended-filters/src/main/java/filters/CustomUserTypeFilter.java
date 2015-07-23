package filters;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;
import com.wixpress.petri.experiments.domain.FilterTypeName;
import com.wixpress.petri.laboratory.EligibilityCriteriaTypes.CustomContextCriterion;

import java.util.Map;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@FilterTypeName("UserType")
public class CustomUserTypeFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        Map<String, String> customContext = eligibilityCriteria.getAdditionalCriterion(CustomContextCriterion.class);

        return "special".equals(customContext.get("userType"));
    }
}
