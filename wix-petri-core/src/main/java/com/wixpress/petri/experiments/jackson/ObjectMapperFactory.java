package com.wixpress.petri.experiments.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import org.joda.time.DateTime;

import java.util.TimeZone;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 7/7/14
* Time: 1:51 PM
* To change this template use File | Settings | File Templates.
*/
public class ObjectMapperFactory {

    private static final String MODULE_NAME = JodaModule.class.getName();
    private static final Version MODULE_VERSION = new Version(1, 0, 0, null, "none", "none");

    public static ObjectMapper makeObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule(MODULE_NAME, MODULE_VERSION);

        module.addDeserializer(DateTime.class, new TimeZoneAwareDateTimeDeserializer());
        module.addSerializer(DateTime.class, new DateTimeSerializer());
        objectMapper.registerModule(module);
        objectMapper.registerModule(new DefaultScalaModule());

        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }
}
