package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class DagpengeAndelLagtTilBesteberegningDto {

    @JsonProperty("fastsatteVerdier")
    @Valid
    private FastsatteVerdierForBesteberegningDto fastsatteVerdier;

    public DagpengeAndelLagtTilBesteberegningDto() {
        // For json deserialisering
    }

    public DagpengeAndelLagtTilBesteberegningDto(int fastsattBeløp, Inntektskategori inntektskategori) {
        this.fastsatteVerdier = new FastsatteVerdierForBesteberegningDto(fastsattBeløp, inntektskategori);
    }

    public FastsatteVerdierForBesteberegningDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

}
