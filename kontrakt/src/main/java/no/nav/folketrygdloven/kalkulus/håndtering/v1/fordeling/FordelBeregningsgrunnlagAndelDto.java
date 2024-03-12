package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FordelBeregningsgrunnlagAndelDto extends FordelRedigerbarAndelDto {

    @JsonProperty("fastsatteVerdier")
    @Valid
    @NotNull
    private FordelFastsatteVerdierDto fastsatteVerdier;

    @JsonProperty("forrigeInntektskategori")
    @Valid
    private Inntektskategori forrigeInntektskategori;

    @JsonProperty("forrigeRefusjonPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer forrigeRefusjonPrÅr;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer forrigeArbeidsinntektPrÅr;

    FordelBeregningsgrunnlagAndelDto() { // NOSONAR
        // Jackson
    }


    public FordelBeregningsgrunnlagAndelDto(@NotNull @Valid FordelRedigerbarAndelDto andelDto,
                                            @NotNull @Valid FordelFastsatteVerdierDto fastsatteVerdier,
                                            @Valid Inntektskategori forrigeInntektskategori,
                                            @Valid Integer forrigeRefusjonPrÅr,
                                            @Valid Integer forrigeArbeidsinntektPrÅr) {
        super(andelDto.getAndelsnr(), andelDto.getArbeidsgiverId(), andelDto.getArbeidsforholdId().getAbakusReferanse(),
                andelDto.getNyAndel(), andelDto.getKilde());
        this.fastsatteVerdier = fastsatteVerdier;
        this.forrigeArbeidsinntektPrÅr = forrigeArbeidsinntektPrÅr;
        this.forrigeInntektskategori = forrigeInntektskategori;
        this.forrigeRefusjonPrÅr = forrigeRefusjonPrÅr;
    }

    public FordelFastsatteVerdierDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

    public Inntektskategori getForrigeInntektskategori() {
        return forrigeInntektskategori;
    }

    public Integer getForrigeRefusjonPrÅr() {
        return forrigeRefusjonPrÅr;
    }

    public Integer getForrigeArbeidsinntektPrÅr() {
        return forrigeArbeidsinntektPrÅr;
    }
}
