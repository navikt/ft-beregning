package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelDto {

    @JsonProperty(value = "andelsnr")
    @Min(1)
    @Max(100)
    @Valid
    private Long andelsnr;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "beregningsperiode")
    @NotNull
    @Valid
    private Periode beregningsperiode;

    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    private Beløp bruttoPrÅr;

    @JsonProperty(value = "redusertRefusjonPrÅr")
    @Valid
    private Beløp redusertRefusjonPrÅr;

    @JsonProperty(value = "redusertBrukersAndelPrÅr")
    @Valid
    private Beløp redusertBrukersAndelPrÅr;

    @JsonProperty(value = "dagsatsBruker")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsatsBruker;

    @JsonProperty(value = "dagsatsArbeidsgiver")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsatsArbeidsgiver;

    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "bgAndelArbeidsforhold")
    @Valid
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    @JsonProperty(value = "overstyrtPrÅr")
    @Valid
    private Beløp overstyrtPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @Valid
    private Beløp avkortetPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @Valid
    private Beløp redusertPrÅr;

    @JsonProperty(value = "beregnetPrÅr")
    @Valid
    private Beløp beregnetPrÅr;

    @JsonProperty(value = "besteberegningPrÅr")
    @Valid
    private Beløp besteberegningPrÅr;

    @JsonProperty(value = "fordeltPrÅr")
    @Valid
    private Beløp fordeltPrÅr;

    @JsonProperty(value = "manueltFordeltPrÅr")
    @Valid
    private Beløp manueltFordeltPrÅr;

    @JsonProperty(value = "maksimalRefusjonPrÅr")
    @Valid
    private Beløp maksimalRefusjonPrÅr;

    @JsonProperty(value = "avkortetRefusjonPrÅr")
    @Valid
    private Beløp avkortetRefusjonPrÅr;

    @JsonProperty(value = "avkortetBrukersAndelPrÅr")
    @Valid
    private Beløp avkortetBrukersAndelPrÅr;

    @JsonProperty(value = "pgiSnitt")
    @Valid
    private Beløp pgiSnitt;

    @JsonProperty(value = "pgi1")
    @Valid
    private Beløp pgi1;

    @JsonProperty(value = "pgi2")
    @Valid
    private Beløp pgi2;

    @JsonProperty(value = "pgi3")
    @Valid
    private Beløp pgi3;

    @JsonProperty(value = "årsbeløpFraTilstøtendeYtelse")
    @Valid
    private Beløp årsbeløpFraTilstøtendeYtelse;

    @JsonProperty(value = "fastsattAvSaksbehandler")
    @Valid
    private Boolean fastsattAvSaksbehandler = false;

    @JsonProperty(value = "lagtTilAvSaksbehandler")
    @Valid
    private Boolean lagtTilAvSaksbehandler = false;

    @JsonProperty(value = "orginalDagsatsFraTilstøtendeYtelse")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long orginalDagsatsFraTilstøtendeYtelse;

    // TODO Fjern dette feltet når det er laget en egen brevtjeneste
    @JsonProperty(value = "avkortetMotInntektstak")
    @Valid
    private Beløp avkortetMotInntektstak;

    // TODO Fjern dette feltet når det er laget en egen brevtjeneste
    @JsonProperty(value = "avkortetFørGraderingPrÅr")
    @Valid
    private Beløp avkortetFørGraderingPrÅr;


    public static class Builder {
        private BeregningsgrunnlagPrStatusOgAndelDto kladd = new BeregningsgrunnlagPrStatusOgAndelDto();

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medBeregningsperiode(Periode beregningsperiode) {
            kladd.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
            kladd.arbeidsforholdType = arbeidsforholdType;
            return this;
        }

        public Builder medBruttoPrÅr(Beløp bruttoPrÅr) {
            kladd.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(Beløp redusertRefusjonPrÅr) {
            kladd.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(Beløp redusertBrukersAndelPrÅr) {
            kladd.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            return this;
        }

        public Builder medDagsatsBruker(Long dagsatsBruker) {
            kladd.dagsatsBruker = dagsatsBruker;
            return this;
        }

        public Builder medDagsatsArbeidsgiver(Long dagsatsArbeidsgiver) {
            kladd.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medBgAndelArbeidsforhold(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
            kladd.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
            return this;
        }

        public Builder medOverstyrtPrÅr(Beløp overstyrtPrÅr) {
            kladd.overstyrtPrÅr = overstyrtPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medBeregnetPrÅr(Beløp beregnetPrÅr) {
            kladd.beregnetPrÅr = beregnetPrÅr;
            return this;
        }

        public Builder medBesteberegningPrÅr(Beløp besteberegningPrÅr) {
            kladd.besteberegningPrÅr = besteberegningPrÅr;
            return this;
        }

        public Builder medFordeltPrÅr(Beløp fordeltPrÅr) {
            kladd.fordeltPrÅr = fordeltPrÅr;
            return this;
        }

        public Builder medManueltFordeltPrÅr(Beløp manueltFordeltPrÅr) {
            kladd.manueltFordeltPrÅr = manueltFordeltPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(Beløp maksimalRefusjonPrÅr) {
            kladd.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(Beløp avkortetRefusjonPrÅr) {
            kladd.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(Beløp avkortetBrukersAndelPrÅr) {
            kladd.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medPgiSnitt(Beløp pgiSnitt) {
            kladd.pgiSnitt = pgiSnitt;
            return this;
        }

        public Builder medPgi1(Beløp pgi1) {
            kladd.pgi1 = pgi1;
            return this;
        }

        public Builder medPgi2(Beløp pgi2) {
            kladd.pgi2 = pgi2;
            return this;
        }

        public Builder medPgi3(Beløp pgi3) {
            kladd.pgi3 = pgi3;
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(Beløp årsbeløpFraTilstøtendeYtelse) {
            kladd.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            kladd.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            kladd.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            return this;
        }

        public Builder medOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
            kladd.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
            return this;
        }

        public Builder medAvkortetMotInntektstak(Beløp avkortetMotInntektstak) {
            kladd.avkortetMotInntektstak = avkortetMotInntektstak;
            return this;
        }

        public Builder medAvkortetFørGraderingPrÅr(Beløp avkortetFørGraderingPrÅr) {
            kladd.avkortetFørGraderingPrÅr = avkortetFørGraderingPrÅr;
            return this;
        }

        public BeregningsgrunnlagPrStatusOgAndelDto build() {
            return kladd;
        }

    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFom() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTom() : null;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Long getDagsats() {
        if (dagsatsBruker == null) {
            return dagsatsArbeidsgiver;
        }
        if (dagsatsArbeidsgiver == null) {
            return dagsatsBruker;
        }
        return dagsatsBruker + dagsatsArbeidsgiver;
    }

    public BGAndelArbeidsforhold getBgAndelArbeidsforhold() {
        return bgAndelArbeidsforhold;
    }


    public Arbeidsgiver getArbeidsgiver() {
        BGAndelArbeidsforhold beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold == null ? null : beregningArbeidsforhold.getArbeidsgiver();
    }

    public Beløp getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Beløp getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public Beløp getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public Beløp getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public Beløp getManueltFordeltPrÅr() {
        return manueltFordeltPrÅr;
    }

    public Beløp getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public Beløp getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public Beløp getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public Beløp getPgiSnitt() {
        return pgiSnitt;
    }

    public Beløp getPgi1() {
        return pgi1;
    }

    public Beløp getPgi2() {
        return pgi2;
    }

    public Beløp getPgi3() {
        return pgi3;
    }

    public Beløp getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Beløp getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public void setAvkortetMotInntektstak(Beløp avkortetMotInntektstak) {
        this.avkortetMotInntektstak = avkortetMotInntektstak;
    }

    public Beløp getAvkortetMotInntektstak() {
        return avkortetMotInntektstak;
    }

    public Beløp getAvkortetFørGraderingPrÅr() {
        return avkortetFørGraderingPrÅr;
    }

    public void setAvkortetFørGraderingPrÅr(Beløp avkortetFørGraderingPrÅr) {
        this.avkortetFørGraderingPrÅr = avkortetFørGraderingPrÅr;
    }
}
