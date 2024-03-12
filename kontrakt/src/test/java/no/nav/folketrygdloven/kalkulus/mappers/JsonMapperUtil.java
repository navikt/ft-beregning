package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;

import org.assertj.core.api.Assertions;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonMapperUtil {

    public static final ObjectWriter WRITER_JSON = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    public static final ObjectReader READER_JSON = JsonMapper.getMapper().reader();

    public static void validateResult(Object roundTripped) {
        Assertions.assertThat(roundTripped).isNotNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(roundTripped);
            assertThat(violations).isEmpty();
        }
    }
}
