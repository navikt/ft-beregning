package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


public class BeregningsgrunnlagPrStatusOgAndelMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Digits(integer = 4, fraction = 0)
    private Long andelsnr;

    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @Valid
    private Periode beregningsperiode;

    @Valid
    @NotNull
    private OpptjeningAktivitetType arbeidsforholdType;

    @Valid
    private Beløp beregnetPrÅr;

    @Valid
    private Beløp fordeltPrÅr;

    @Valid
    private Beløp manueltFordeltPrÅr;

    @Valid
    private Beløp overstyrtPrÅr;

    @Valid
    private Beløp besteberegningPrÅr;

    @Valid
    private Beløp bruttoPrÅr;

    @Valid
    private Beløp avkortetPrÅr;

    @Valid
    private Beløp redusertPrÅr;

    @Valid
    private Beløp maksimalRefusjonPrÅr;

    @Valid
    private Beløp avkortetRefusjonPrÅr;

    @Valid
    private Beløp redusertRefusjonPrÅr;

    @Valid
    private Beløp avkortetBrukersAndelPrÅr;

    @Valid
    private Beløp redusertBrukersAndelPrÅr;

    @Valid
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "4000.00")
    @Digits(integer = 4, fraction = 0)
    private Long dagsatsBruker;

    @Valid
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "4000.00")
    @Digits(integer = 4, fraction = 0)
    private Long dagsatsArbeidsgiver;

    @Valid
    private Beløp pgiSnitt;

    @Valid
    private Beløp pgi1;

    @Valid
    private Beløp pgi2;

    @Valid
    private Beløp pgi3;

    @Valid
    private Beløp årsbeløpFraTilstøtendeYtelse;

    @Valid
    @NotNull
    private Boolean fastsattAvSaksbehandler;

    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    @Valid
    private Inntektskategori inntektskategoriAutomatiskFordeling;

    @Valid
    private Inntektskategori inntektskategoriManuellFordeling;

    @Valid
    @NotNull
    private AndelKilde kilde;

    @Valid
    private BGAndelArbeidsforholdMigreringDto bgAndelArbeidsforhold;

    @Valid
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "3000.00")
    @Digits(integer = 4, fraction = 0)
    private Long orginalDagsatsFraTilstøtendeYtelse;

    public BeregningsgrunnlagPrStatusOgAndelMigreringDto() {
        // Bruker heller settere her siden det er så mange like felter
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public void setBeregningsperiode(Periode beregningsperiode) {
        this.beregningsperiode = beregningsperiode;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public void setBeregnetPrÅr(Beløp beregnetPrÅr) {
        this.beregnetPrÅr = beregnetPrÅr;
    }

    public void setFordeltPrÅr(Beløp fordeltPrÅr) {
        this.fordeltPrÅr = fordeltPrÅr;
    }

    public void setManueltFordeltPrÅr(Beløp manueltFordeltPrÅr) {
        this.manueltFordeltPrÅr = manueltFordeltPrÅr;
    }

    public void setOverstyrtPrÅr(Beløp overstyrtPrÅr) {
        this.overstyrtPrÅr = overstyrtPrÅr;
    }

    public void setBesteberegningPrÅr(Beløp besteberegningPrÅr) {
        this.besteberegningPrÅr = besteberegningPrÅr;
    }

    public void setBruttoPrÅr(Beløp bruttoPrÅr) {
        this.bruttoPrÅr = bruttoPrÅr;
    }

    public void setAvkortetPrÅr(Beløp avkortetPrÅr) {
        this.avkortetPrÅr = avkortetPrÅr;
    }

    public void setRedusertPrÅr(Beløp redusertPrÅr) {
        this.redusertPrÅr = redusertPrÅr;
    }

    public void setMaksimalRefusjonPrÅr(Beløp maksimalRefusjonPrÅr) {
        this.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
    }

    public void setAvkortetRefusjonPrÅr(Beløp avkortetRefusjonPrÅr) {
        this.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
    }

    public void setRedusertRefusjonPrÅr(Beløp redusertRefusjonPrÅr) {
        this.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
    }

    public void setAvkortetBrukersAndelPrÅr(Beløp avkortetBrukersAndelPrÅr) {
        this.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
    }

    public void setRedusertBrukersAndelPrÅr(Beløp redusertBrukersAndelPrÅr) {
        this.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
    }

    public void setDagsatsBruker(Long dagsatsBruker) {
        this.dagsatsBruker = dagsatsBruker;
    }

    public void setDagsatsArbeidsgiver(Long dagsatsArbeidsgiver) {
        this.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
    }

    public void setPgiSnitt(Beløp pgiSnitt) {
        this.pgiSnitt = pgiSnitt;
    }

    public void setPgi1(Beløp pgi1) {
        this.pgi1 = pgi1;
    }

    public void setPgi2(Beløp pgi2) {
        this.pgi2 = pgi2;
    }

    public void setPgi3(Beløp pgi3) {
        this.pgi3 = pgi3;
    }

    public void setÅrsbeløpFraTilstøtendeYtelse(Beløp årsbeløpFraTilstøtendeYtelse) {
        this.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
    }

    public void setFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public void setInntektskategoriAutomatiskFordeling(Inntektskategori inntektskategoriAutomatiskFordeling) {
        this.inntektskategoriAutomatiskFordeling = inntektskategoriAutomatiskFordeling;
    }

    public void setInntektskategoriManuellFordeling(Inntektskategori inntektskategoriManuellFordeling) {
        this.inntektskategoriManuellFordeling = inntektskategoriManuellFordeling;
    }

    public void setKilde(AndelKilde kilde) {
        this.kilde = kilde;
    }

    public void setBgAndelArbeidsforhold(BGAndelArbeidsforholdMigreringDto bgAndelArbeidsforhold) {
        this.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
    }

    public void setOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
        this.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Periode getBeregningsperiode() {
        return beregningsperiode;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
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

    public Beløp getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public Beløp getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public Beløp getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public Beløp getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public Beløp getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public Beløp getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
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

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Inntektskategori getInntektskategoriAutomatiskFordeling() {
        return inntektskategoriAutomatiskFordeling;
    }

    public Inntektskategori getInntektskategoriManuellFordeling() {
        return inntektskategoriManuellFordeling;
    }

    public AndelKilde getKilde() {
        return kilde;
    }

    public BGAndelArbeidsforholdMigreringDto getBgAndelArbeidsforhold() {
        return bgAndelArbeidsforhold;
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }
}
