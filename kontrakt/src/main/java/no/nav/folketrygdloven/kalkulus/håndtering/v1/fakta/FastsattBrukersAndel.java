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
public class FastsattBrukersAndel {

    @JsonProperty("andelsnr")
    @Valid
    private Long andelsnr;

    @JsonProperty("nyAndel")
    @Valid
    @NotNull
    private Boolean nyAndel;

    @JsonProperty("lagtTilAvSaksbehandler")
    @Valid
    @NotNull
    private Boolean lagtTilAvSaksbehandler;

    @JsonProperty("fastsattBeløp")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer fastsattBeløp;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    public FastsattBrukersAndel() {
    }

    public FastsattBrukersAndel(@Valid Long andelsnr, @Valid @NotNull Boolean nyAndel, @Valid @NotNull Boolean lagtTilAvSaksbehandler, @Valid @NotNull Integer fastsattBeløp, @Valid @NotNull Inntektskategori inntektskategori) {
        this.andelsnr = andelsnr;
        this.nyAndel = nyAndel;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
