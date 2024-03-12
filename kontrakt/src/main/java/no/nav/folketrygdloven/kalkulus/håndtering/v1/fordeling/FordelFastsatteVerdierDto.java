package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

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
public class FordelFastsatteVerdierDto {

    @JsonProperty("refusjonPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer refusjonPrÅr;

    @JsonProperty("fastsattÅrsbeløp")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer fastsattÅrsbeløp;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    @JsonProperty("fastsattÅrsbeløpInklNaturalytelse")
    @Min(0)
    @Max(178956970)
    @NotNull
    private Integer fastsattÅrsbeløpInklNaturalytelse;

    public FordelFastsatteVerdierDto() {
        // For json deserialisering
    }


    public FordelFastsatteVerdierDto(@Valid @Min(0) @Max(178956970) Integer refusjonPrÅr,
                                     @Valid @Min(0) @Max(178956970) Integer fastsattÅrsbeløp,
                                     @Valid @NotNull Inntektskategori inntektskategori,
                                     @Min(0) @Max(178956970) @NotNull Integer fastsattÅrsbeløpInklNaturalytelse) {
        this.refusjonPrÅr = refusjonPrÅr;
        this.fastsattÅrsbeløp = fastsattÅrsbeløp;
        this.inntektskategori = inntektskategori;
        this.fastsattÅrsbeløpInklNaturalytelse = fastsattÅrsbeløpInklNaturalytelse;
    }

    public Integer getRefusjonPrÅr() {
        return refusjonPrÅr;
    }

    public Integer getFastsattÅrsbeløp() {
        return fastsattÅrsbeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Integer getFastsattÅrsbeløpInklNaturalytelse() {
        return fastsattÅrsbeløpInklNaturalytelse;
    }
}
