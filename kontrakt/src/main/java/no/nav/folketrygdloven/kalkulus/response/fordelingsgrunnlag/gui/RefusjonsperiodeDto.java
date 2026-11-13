package no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import no.nav.folketrygdloven.kalkulus.felles.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RefusjonsperiodeDto(
        @NotNull LocalDate fom,
        @NotNull LocalDate tom,
        @NotNull @Valid Beløp refusjonPrMnd,
        @NotNull @Valid RefusjonUtfall utfall,
        @Valid RefusjonUtfallÅrsak utfallÅrsak) {}
