package com.wixpress.guineapig.services;

import com.wixpress.guineapig.dto.UserAgentRegex;
import com.wixpress.guineapig.entities.ui.FilterOption;

import java.util.List;

public interface EditableMetaDataService {

    List<FilterOption> getUserAgentRegexList();

    void addUserAgentRegex(UserAgentRegex userAgentRegex);

    void deleteUserAgentRegex(String regex);
}
