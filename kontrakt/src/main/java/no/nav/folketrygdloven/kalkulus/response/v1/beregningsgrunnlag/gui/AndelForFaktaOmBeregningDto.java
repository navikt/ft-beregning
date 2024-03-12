package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class AndelForFaktaOmBeregningDto {

    @Valid
    @JsonProperty(value = "belopReadOnly")
    private Beløp belopReadOnly;

    @Valid
    @JsonProperty(value = "fastsattBelop")
    private Beløp fastsattBelop;

    @Valid
    @JsonProperty(value = "inntektskategori")
    private Inntektskategori inntektskategori;

    @Valid
    @JsonProperty(value = "aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty(value = "refusjonskrav")
    private Beløp refusjonskrav;

    @Valid
    @JsonProperty(value = "arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @Valid
    @JsonProperty(value = "andelsnr")
    @Min(0)
    @Max(1000)
    private Long andelsnr;

    @Valid
    @JsonProperty(value = "skalKunneEndreAktivitet")
    private Boolean skalKunneEndreAktivitet;

    @Valid
    @JsonProperty(value = "lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler;

    public BeregningsgrunnlagArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }

    public void setArbeidsforhold(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public Beløp getRefusjonskrav() {
        return refusjonskrav;
    }

    public void setRefusjonskrav(Beløp refusjonskrav) {
        this.refusjonskrav = refusjonskrav;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public Beløp getBelopReadOnly() {
        return belopReadOnly;
    }

    public void setBelopReadOnly(Beløp belopReadOnly) {
        this.belopReadOnly = belopReadOnly;
    }

    public Beløp getFastsattBelop() {
        return fastsattBelop;
    }

    public void setFastsattBelop(Beløp fastsattBelop) {
        this.fastsattBelop = fastsattBelop;
    }

    public Boolean getSkalKunneEndreAktivitet() {
        return skalKunneEndreAktivitet;
    }

    public void setSkalKunneEndreAktivitet(Boolean skalKunneEndreAktivitet) {
        this.skalKunneEndreAktivitet = skalKunneEndreAktivitet;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }
}
