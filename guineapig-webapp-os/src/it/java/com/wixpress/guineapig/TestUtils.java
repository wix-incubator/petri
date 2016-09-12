package com.wixpress.guineapig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wixpress.guineapig.drivers.JsonResponse;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;

import java.io.IOException;

/**
 * User: Dalias
 * Date: 10/26/14
 * Time: 4:01 PM
 */
public class TestUtils {

    public static <T extends Iterable> T extractCollectionPayload(JsonResponse response, TypeReference<T> typeReference) throws IOException {
        return ObjectMapperFactory.makeObjectMapper().readValue(response.getBodyJson().toString(), typeReference);
    }

}
