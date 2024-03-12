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
public class FastsettMånedsinntektUtenInntektsmeldingAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @Min(0)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("fastsattBeløp")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer fastsattBeløp;

    @JsonProperty("inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    public FastsettMånedsinntektUtenInntektsmeldingAndelDto() {
    }

    public FastsettMånedsinntektUtenInntektsmeldingAndelDto(@Valid @NotNull Long andelsnr, @Valid @NotNull Integer fastsattBeløp, @Valid Inntektskategori inntektskategori) {
        this.andelsnr = andelsnr;
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }


    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

}
