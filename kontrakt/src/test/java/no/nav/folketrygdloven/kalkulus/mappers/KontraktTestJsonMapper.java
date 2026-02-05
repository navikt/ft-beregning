package no.nav.folketrygdloven.kalkulus.mappers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class KontraktTestJsonMapper {
    private KontraktTestJsonMapper() {
        /* This utility class should not be instantiated */
    }


    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .addModules(new Jdk8Module(), new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
        .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
        .build();
    public static final ObjectWriter WRITER_JSON = KontraktTestJsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    public static final ObjectReader READER_JSON = KontraktTestJsonMapper.getMapper().reader();



    public static JsonMapper getMapper() {
        return JSON_MAPPER;
    }


}
