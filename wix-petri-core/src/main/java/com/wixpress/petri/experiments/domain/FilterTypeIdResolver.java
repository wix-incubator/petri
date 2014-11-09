package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: talyag
 * @since: 11/27/13
 */
public class FilterTypeIdResolver implements TypeIdResolver {

    private JavaType baseType;

    private Map<String, Class<? extends Filter>> typeIdsMap = new HashMap<String, Class<? extends Filter>>();
    private Map<Class<? extends Filter>, String> typesMap = new HashMap<Class<? extends Filter>, String>();

    // IMPORTANT - The keys here CANNOT be changed. They are the unique identifiers and enable you to rename your filter class if you like.
    public FilterTypeIdResolver() {
        registerTypeWithId("geo", GeoFilter.class);
        registerTypeWithId("anonymous", FirstTimeVisitorsOnlyFilter.class);
        registerTypeWithId("registered", RegisteredUsersFilter.class);
        registerTypeWithId("newUsers", NewUsersFilter.class);
        registerTypeWithId("wixUsers", WixEmployeesFilter.class);
        registerTypeWithId("language", LanguageFilter.class);
        registerTypeWithId("host", HostFilter.class);
        registerTypeWithId("includeUserIds", IncludeUserIdsFilter.class);
        registerTypeWithId("not", NotFilter.class);
        registerTypeWithId("browserVersion", BrowserVersionFilter.class);
        registerTypeWithId("userAgentRegex", UserAgentRegexFilter.class);
    }

    private void registerTypeWithId(String typeId, Class<? extends Filter> filterClass) {
        typeIdsMap.put(typeId, filterClass);
        typesMap.put(filterClass, typeId);
    }

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
        for (Map.Entry<String, Class<? extends Filter>> entry : ExtendedFilterTypesIds.extendedTypes()) {
            registerTypeWithId(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return typesMap.get(suggestedType);
    }

    @Override
    public String idFromBaseType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JavaType typeFromId(String id) {
        if (!typeIdsMap.containsKey(id)) {
            return TypeFactory.defaultInstance().constructType(UnrecognizedFilter.class);
        }
        return TypeFactory.defaultInstance().constructSpecializedType(baseType, typeIdsMap.get(id));
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
