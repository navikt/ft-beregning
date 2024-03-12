package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsatteVerdierDto {

    @JsonProperty("fastsattBeløpPrMnd")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer fastsattBeløpPrMnd;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    @JsonProperty("skalHaBesteberegning")
    @Valid
    private Boolean skalHaBesteberegning;

    public FastsatteVerdierDto() {
        // For json deserialisering
    }

    public FastsatteVerdierDto(@Valid @Min(0) @Max(178956970) Integer fastsattBeløpPrMnd,
                               @Valid @NotNull Inntektskategori inntektskategori,
                               @Valid Boolean skalHaBesteberegning) {
        this.fastsattBeløpPrMnd = fastsattBeløpPrMnd;
        this.inntektskategori = inntektskategori;
        this.skalHaBesteberegning = skalHaBesteberegning;
    }

    public Integer getFastsattBeløpPrMnd() { return fastsattBeløpPrMnd; }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Boolean getSkalHaBesteberegning() {
        return skalHaBesteberegning;
    }
}
