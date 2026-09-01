package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

public class KontraktTestJsonMapper {
    private KontraktTestJsonMapper() {
        /* This utility class should not be instantiated */
    }


    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .defaultTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // Var noen tester med null for booleans
        .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .changeDefaultPropertyInclusion(a -> a
            .withValueInclusion(JsonInclude.Include.NON_NULL)
            .withContentInclusion(JsonInclude.Include.NON_NULL))
        .changeDefaultVisibility(v -> v
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
            .withScalarConstructorVisibility(JsonAutoDetect.Visibility.ANY))
        .build();

    public static final ObjectWriter WRITER_JSON = KontraktTestJsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    public static final ObjectReader READER_JSON = KontraktTestJsonMapper.getMapper().reader();



    public static JsonMapper getMapper() {
        return JSON_MAPPER;
    }


}
