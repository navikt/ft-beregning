package no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.annoteringer.Fritekst;
import no.nav.folketrygdloven.kalkulus.felles.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RefusjonsperiodeDto(
        @JsonProperty(value = "periode") @Valid Periode periode,
        @JsonProperty(value = "refusjonPrMnd") @Valid Beløp refusjonPrMnd,
        @JsonProperty(value = "utfall") @Valid RefusjonUtfall utfall,
        @JsonProperty(value = "utfallÅrsak") @Valid RefusjonUtfallÅrsak utfallÅrsak,
        @JsonProperty(value = "utfallBegrunnelse") @Fritekst String utfallBegrunnelse) {}
