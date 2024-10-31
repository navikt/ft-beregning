package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.time.LocalDate;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.folketrygdloven.kalkulus.kodeverk.MidlertidigInaktivType;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

import static org.assertj.core.api.Assertions.assertThat;

class OpptjeningAktiviteterDtoTest {

	@Test
	void serialisering() throws JsonProcessingException {
		var opptjeningDto = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, new Periode(LocalDate.now(), LocalDate.now().plusDays(10)))));
		var om = createObjectMapper();
		var string = om.writeValueAsString(opptjeningDto);
		assertThat(string).isNotBlank();
	}

	@Test
	void serialiseringSecondCtor() throws JsonProcessingException {
		var opptjeningDto = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, new Periode(LocalDate.now(), LocalDate.now().plusDays(10)))), MidlertidigInaktivType.A);
		var om = createObjectMapper();
		var string = om.writeValueAsString(opptjeningDto);
		assertThat(string).isNotBlank();
	}

	@Test
	void deserialisering() throws JsonProcessingException {
		var json = "{\"perioder\":[{\"opptjeningAktivitetType\":\"ARBEID\",\"periode\":{\"fom\":\"2024-10-31\",\"tom\":\"2024-11-10\"}}]}";
		var mapper = createObjectMapper();
		var opptjeningAktivitet = mapper.readValue(json, OpptjeningAktiviteterDto.class);
		assertThat(opptjeningAktivitet).isNotNull();
		assertThat(opptjeningAktivitet.getPerioder()).isNotNull().hasSize(1);
		assertThat(opptjeningAktivitet.getMidlertidigInaktivType()).isNull();
	}

	@Test
	void deserialiseringSecondCtor() throws JsonProcessingException {
		var json = "{\"perioder\":[{\"opptjeningAktivitetType\":\"ARBEID\",\"periode\":{\"fom\":\"2024-10-31\",\"tom\":\"2024-11-10\"}}],\"midlertidigInaktivType\":\"8-47 A\"}";

		var mapper = createObjectMapper();
		var opptjeningAktivitet = mapper.readValue(json, OpptjeningAktiviteterDto.class);
		assertThat(opptjeningAktivitet).isNotNull();
		assertThat(opptjeningAktivitet.getPerioder()).isNotNull().hasSize(1);
		assertThat(opptjeningAktivitet.getMidlertidigInaktivType()).isNotNull();
	}

	private static ObjectMapper createObjectMapper() {
		return new ObjectMapper().registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule())
				.setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
				.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
	}
}