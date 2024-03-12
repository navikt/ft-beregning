package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.FastsattInntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class BeregningsgrunnlagPrStatusOgAndelDto implements IndexKey {

    private Long andelsnr;
    @DiffIgnore
    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;

    @SjekkVedKopiering
    private AktivitetStatus aktivitetStatus;
    @SjekkVedKopiering
    private Intervall beregningsperiode;
    private OpptjeningAktivitetType arbeidsforholdType;
    @SjekkVedKopiering
    private Årsgrunnlag grunnlagPrÅr = new Årsgrunnlag();
    @SjekkVedKopiering
    private Beløp avkortetPrÅr;
    @SjekkVedKopiering
    private Beløp redusertPrÅr;
    @SjekkVedKopiering
    private Beløp maksimalRefusjonPrÅr;
    @SjekkVedKopiering
    private Beløp avkortetRefusjonPrÅr;
    @SjekkVedKopiering
    private Beløp redusertRefusjonPrÅr;
    @SjekkVedKopiering
    private Beløp avkortetBrukersAndelPrÅr;
    @SjekkVedKopiering
    private Beløp redusertBrukersAndelPrÅr;
    @SjekkVedKopiering
    private Long dagsatsBruker;
    @SjekkVedKopiering
    private Long dagsatsArbeidsgiver;
    @SjekkVedKopiering
    private Beløp avkortetFørGraderingPrÅr;
    @SjekkVedKopiering
    private Beløp pgiSnitt;
    @SjekkVedKopiering
    private Beløp pgi1;
    @SjekkVedKopiering
    private Beløp pgi2;
    @SjekkVedKopiering
    private Beløp pgi3;
    @SjekkVedKopiering
    private Beløp årsbeløpFraTilstøtendeYtelse;
    @SjekkVedKopiering
    private Boolean fastsattAvSaksbehandler = false;
    @SjekkVedKopiering
    private FastsattInntektskategori fastsattInntektskategori = new FastsattInntektskategori();
    @SjekkVedKopiering
    private AndelKilde kilde = AndelKilde.PROSESS_START;
    @SjekkVedKopiering
    private BGAndelArbeidsforholdDto bgAndelArbeidsforhold;
    @SjekkVedKopiering
    private Long orginalDagsatsFraTilstøtendeYtelse;

    public BeregningsgrunnlagPrStatusOgAndelDto() {
    }

    public BeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagPrStatusOgAndelDto kopiereFra) {
        this.andelsnr = kopiereFra.andelsnr;
        this.aktivitetStatus = kopiereFra.aktivitetStatus;
        this.beregningsperiode = kopiereFra.beregningsperiode;
        this.arbeidsforholdType = kopiereFra.arbeidsforholdType;
        this.grunnlagPrÅr = new Årsgrunnlag(kopiereFra.grunnlagPrÅr);
        this.avkortetPrÅr = kopiereFra.avkortetPrÅr;
        this.redusertPrÅr = kopiereFra.redusertPrÅr;
        this.maksimalRefusjonPrÅr = kopiereFra.maksimalRefusjonPrÅr;
        this.avkortetRefusjonPrÅr = kopiereFra.avkortetRefusjonPrÅr;
        this.redusertRefusjonPrÅr = kopiereFra.redusertRefusjonPrÅr;
        this.avkortetBrukersAndelPrÅr = kopiereFra.avkortetBrukersAndelPrÅr;
        this.redusertBrukersAndelPrÅr = kopiereFra.redusertBrukersAndelPrÅr;
        this.dagsatsBruker = kopiereFra.dagsatsBruker;
        this.dagsatsArbeidsgiver = kopiereFra.dagsatsArbeidsgiver;
        this.pgiSnitt = kopiereFra.pgiSnitt;
        this.pgi1 = kopiereFra.pgi1;
        this.pgi2 = kopiereFra.pgi2;
        this.pgi3 = kopiereFra.pgi3;
        this.årsbeløpFraTilstøtendeYtelse = kopiereFra.årsbeløpFraTilstøtendeYtelse;
        this.fastsattAvSaksbehandler = kopiereFra.fastsattAvSaksbehandler;
        this.fastsattInntektskategori = new FastsattInntektskategori(kopiereFra.fastsattInntektskategori);
        this.kilde = kopiereFra.kilde;
        this.orginalDagsatsFraTilstøtendeYtelse = kopiereFra.orginalDagsatsFraTilstøtendeYtelse;
        this.avkortetFørGraderingPrÅr = kopiereFra.avkortetFørGraderingPrÅr;
        if (kopiereFra.bgAndelArbeidsforhold != null) {
            this.bgAndelArbeidsforhold = BGAndelArbeidsforholdDto.Builder.kopier(kopiereFra.bgAndelArbeidsforhold).build(this);
        }
    }


    public BeregningsgrunnlagPeriodeDto getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFomDato() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTomDato() : null;
    }

    public Intervall getBeregningsperiode() {
        return beregningsperiode;
    }

    public boolean gjelderSammeArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto that) {
        if (!Objects.equals(this.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !Objects.equals(that.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER)) {
            return false;
        }
        return gjelderSammeArbeidsforhold(that.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                that.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()));
    }

    public boolean gjelderSammeArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return gjelderSammeArbeidsforhold(Optional.ofNullable(arbeidsgiver), arbeidsforholdRef);
    }

    public boolean gjelderInntektsmeldingFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforholdOpt = getBgAndelArbeidsforhold();
        if (!Objects.equals(getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !bgAndelArbeidsforholdOpt.isPresent()) {
            return false;
        }
        if (!Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), Optional.of(arbeidsgiver))) {
            return false;
        }
        return bgAndelArbeidsforholdOpt.get().getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    private boolean gjelderSammeArbeidsforhold(Optional<Arbeidsgiver> arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforholdOpt = getBgAndelArbeidsforhold();
        if (!Objects.equals(getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !bgAndelArbeidsforholdOpt.isPresent()) {
            return false;
        }
        return Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), arbeidsgiver)
                && bgAndelArbeidsforholdOpt.get().getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public boolean matchUtenInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto other) {
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef), other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef))
                && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }

    public boolean matchUtenInntektskategori(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internArbeidsforholdRefDto, OpptjeningAktivitetType arbeidsforholdType) {
        return Objects.equals(this.getAktivitetStatus(), aktivitetStatus)
                && this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).map(a -> Objects.equals(a, arbeidsgiver)).orElse(arbeidsgiver == null)
                && this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).map(ref -> Objects.equals(ref, internArbeidsforholdRefDto)).orElse(internArbeidsforholdRefDto == null || internArbeidsforholdRefDto.getReferanse() == null)
                && Objects.equals(this.getArbeidsforholdType(), arbeidsforholdType);
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Beløp getBruttoPrÅr() {
        return grunnlagPrÅr.getBruttoPrÅr();
    }

    public Beløp getBruttoUtenManueltFordelt() {
        return grunnlagPrÅr.getBruttoUtenManueltFordelt();
    }

    public Beløp getOverstyrtPrÅr() {
        return grunnlagPrÅr.getOverstyrtPrÅr();
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getBeregnetPrÅr() {
        return grunnlagPrÅr.getBeregnetPrÅr();
    }

    public Beløp getFordeltPrÅr() {
        return grunnlagPrÅr.getFordeltPrÅr();
    }

    public Beløp getManueltFordeltPrÅr() {
        return grunnlagPrÅr.getManueltFordeltPrÅr();
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

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Inntektskategori getGjeldendeInntektskategori() {
        return fastsattInntektskategori == null ? Inntektskategori.UDEFINERT : fastsattInntektskategori.getGjeldendeInntektskategori();
    }

    public FastsattInntektskategori getFastsattInntektskategori() {
        return fastsattInntektskategori;
    }

    public Årsgrunnlag getGrunnlagPrÅr() {
        return grunnlagPrÅr;
    }

    public Beløp getBruttoInkludertNaturalYtelser() {
        var naturalytelseBortfalt = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr).orElse(Beløp.ZERO);
        var naturalYtelseTilkommet = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr).orElse(Beløp.ZERO);
        var brutto = Optional.ofNullable(grunnlagPrÅr.getBruttoPrÅr()).orElse(Beløp.ZERO);
        return brutto.adder(naturalytelseBortfalt).subtraher(naturalYtelseTilkommet);
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

    public Beløp getÅrsbeløpFraTilstøtendeYtelseVerdi() {
        return Optional.ofNullable(getÅrsbeløpFraTilstøtendeYtelse()).orElse(Beløp.ZERO);
    }

    public Beløp getAvkortetFørGraderingPrÅr() {
        return avkortetFørGraderingPrÅr;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Beløp getBesteberegningPrÅr() {
        return grunnlagPrÅr.getBesteberegningPrÅr();
    }

    public AndelKilde getKilde() {
        return kilde;
    }

    public Boolean erLagtTilAvSaksbehandler() {
        return kilde.equals(AndelKilde.SAKSBEHANDLER_KOFAKBER) || kilde.equals(AndelKilde.SAKSBEHANDLER_FORDELING);
    }

    public Optional<BGAndelArbeidsforholdDto> getBgAndelArbeidsforhold() {
        return Optional.ofNullable(bgAndelArbeidsforhold);
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        Optional<BGAndelArbeidsforholdDto> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforholdDto::getArbeidsgiver);
    }

    public Optional<InternArbeidsforholdRefDto> getArbeidsforholdRef() {
        Optional<BGAndelArbeidsforholdDto> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforholdDto::getArbeidsforholdRef);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPrStatusOgAndelDto)) {
            return false;
        }
        // Endring av denne har store konsekvenser for matching av andeler
        // Resultat av endringer må testes manuelt
        BeregningsgrunnlagPrStatusOgAndelDto other = (BeregningsgrunnlagPrStatusOgAndelDto) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getGjeldendeInntektskategori(), other.getGjeldendeInntektskategori())
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef),
                other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef))
                && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
                fastsattInntektskategori,
                getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef),
                arbeidsforholdType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "beregningsperiode=" + beregningsperiode + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsforholdType=" + arbeidsforholdType + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "maksimalRefusjonPrÅr=" + maksimalRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetRefusjonPrÅr=" + avkortetRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertRefusjonPrÅr=" + redusertRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetBrukersAndelPrÅr=" + avkortetBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertBrukersAndelPrÅr=" + redusertBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsatsBruker=" + dagsatsBruker + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsatsArbeidsgiver=" + dagsatsArbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgiSnitt=" + pgiSnitt + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi1=" + pgi1 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi2=" + pgi2 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi3=" + pgi3 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "årsbeløpFraTilstøtendeYtelse=" + årsbeløpFraTilstøtendeYtelse //$NON-NLS-1$
                + "bgAndelArbeidsforhold=" + bgAndelArbeidsforhold //$NON-NLS-1$
                + "grunnlagPrÅr=" + grunnlagPrÅr //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder ny() {
        return new Builder();
    }

    public static Builder kopier(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndel) {
        return new Builder(eksisterendeBGPrStatusOgAndel);
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeDto) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriodeDto;
    }

    @Override
    public String getIndexKey() {
        return andelsnr.toString();
    }

    public static class Builder {
        /**
         * Når det er built kan ikke denne builderen brukes til annet enn å returnere samme objekt.
         */
        private boolean built;

        private BeregningsgrunnlagPrStatusOgAndelDto kladd;

        private boolean erOppdatering;

        private Builder() {
            kladd = new BeregningsgrunnlagPrStatusOgAndelDto();
            kladd.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
        }

        private Builder(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndelMal) {
            kladd = new BeregningsgrunnlagPrStatusOgAndelDto(eksisterendeBGPrStatusOgAndelMal);
            this.erOppdatering = false;
        }

        private Builder(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndelMal, boolean erOppdatering) {
            kladd = eksisterendeBGPrStatusOgAndelMal;
            if (!erOppdatering) {
                kladd.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
            }
            this.erOppdatering = erOppdatering;
        }

        public static Builder oppdatere(BeregningsgrunnlagPrStatusOgAndelDto oppdatere) {
            return new Builder(oppdatere, true);
        }

        public static Builder ny() {
            return new Builder(new BeregningsgrunnlagPrStatusOgAndelDto(), false);
        }

        public static Builder oppdatere(Optional<BeregningsgrunnlagPrStatusOgAndelDto> oppdatere) {
            return oppdatere.map(Builder::oppdatere).orElseGet(Builder::ny);
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            verifiserKanModifisere();
            kladd.aktivitetStatus = Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
            if (OpptjeningAktivitetType.UDEFINERT.equals(kladd.arbeidsforholdType)) {
                if (AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus)) {
                    kladd.arbeidsforholdType = OpptjeningAktivitetType.ARBEID;
                } else if (AktivitetStatus.FRILANSER.equals(aktivitetStatus)) {
                    kladd.arbeidsforholdType = OpptjeningAktivitetType.FRILANS;
                }
            }
            return this;
        }

        public Builder medBeregningsperiode(LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
            verifiserKanModifisere();
            kladd.beregningsperiode = Intervall.fraOgMedTilOgMed(beregningsperiodeFom, beregningsperiodeTom);
            return this;
        }

        public Builder medBeregningsperiode(Intervall beregningsperiode) {
            verifiserKanModifisere();
            kladd.beregningsperiode = beregningsperiode;
            return this;
        }

        public Builder medArbforholdType(OpptjeningAktivitetType arbforholdType) {
            verifiserKanModifisere();
            kladd.arbeidsforholdType = arbforholdType;
            return this;
        }

        public Builder medOverstyrtPrÅr(Beløp overstyrtPrÅr) {
            verifiserKanModifisere();
            kladd.grunnlagPrÅr.setOverstyrtPrÅr(overstyrtPrÅr);
            oppdaterPeriodebrutto();
            return this;
        }

        private void oppdaterPeriodebrutto() {
            if (kladd.getBeregningsgrunnlagPeriode() != null && kladd.grunnlagPrÅr.getBruttoPrÅr() != null) {
                kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
            }
        }

        public Builder medFordeltPrÅr(Beløp fordeltPrÅr) {
            verifiserKanModifisere();
            kladd.grunnlagPrÅr.setFordeltPrÅr(fordeltPrÅr);
            oppdaterPeriodebrutto();
            return this;
        }

        public Builder medManueltFordeltPrÅr(Beløp fordeltPrÅr) {
            verifiserKanModifisere();
            kladd.grunnlagPrÅr.setManueltFordeltPrÅr(fordeltPrÅr);
            oppdaterPeriodebrutto();
            return this;
        }


        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(Beløp maksimalRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(Beløp avkortetRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(Beløp redusertRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            kladd.dagsatsArbeidsgiver = Optional.ofNullable(Beløp.safeVerdi(redusertRefusjonPrÅr))
                    .map(v -> v.divide(KonfigTjeneste.getYtelsesdagerIÅr(), 0, RoundingMode.HALF_UP).longValue()).orElse(null);
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(Beløp avkortetBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(Beløp redusertBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            kladd.dagsatsBruker = Optional.ofNullable(Beløp.safeVerdi(redusertBrukersAndelPrÅr))
                    .map(b -> b.divide(KonfigTjeneste.getYtelsesdagerIÅr(), 0, RoundingMode.HALF_UP).longValue())
                    .orElse(null);
            return this;
        }

        public Builder medBeregnetPrÅr(Beløp beregnetPrÅr) {
            verifiserKanModifisere();
            kladd.grunnlagPrÅr.setBeregnetPrÅr(beregnetPrÅr);
            oppdaterPeriodebrutto();
            return this;
        }

        public Builder medPgi(Beløp pgiSnitt, List<Beløp> pgiListe) {
            verifiserKanModifisere();
            kladd.pgiSnitt = pgiSnitt;
            kladd.pgi1 = pgiListe.isEmpty() ? null : pgiListe.get(0);
            kladd.pgi2 = pgiListe.isEmpty() ? null : pgiListe.get(1);
            kladd.pgi3 = pgiListe.isEmpty() ? null : pgiListe.get(2);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(Beløp årsbeløpFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
            return this;
        }


        public Builder medAvkortetFørGraderingPrÅr(Beløp avkortetFørGraderingPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetFørGraderingPrÅr = avkortetFørGraderingPrÅr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            verifiserKanModifisere();
            if (kladd.fastsattInntektskategori != null) {
                kladd.fastsattInntektskategori.setInntektskategori(inntektskategori);
            } else {
                kladd.fastsattInntektskategori = new FastsattInntektskategori(inntektskategori, null, null);
            }
            return this;
        }

        public Builder medInntektskategoriAutomatiskFordeling(Inntektskategori inntektskategori) {
            verifiserKanModifisere();
            if (kladd.fastsattInntektskategori != null) {
                kladd.fastsattInntektskategori.setInntektskategoriAutomatiskFordeling(inntektskategori);
            } else {
                kladd.fastsattInntektskategori = new FastsattInntektskategori(null, inntektskategori, null);
            }
            return this;
        }

        public Builder medInntektskategoriManuellFordeling(Inntektskategori inntektskategori) {
            verifiserKanModifisere();
            if (kladd.fastsattInntektskategori != null) {
                kladd.fastsattInntektskategori.setInntektskategoriManuellFordeling(inntektskategori);
            } else {
                kladd.fastsattInntektskategori = new FastsattInntektskategori(null, null, inntektskategori);
            }
            return this;
        }

        public Builder medInntektskategori(FastsattInntektskategori inntektskategori) {
            verifiserKanModifisere();
            kladd.fastsattInntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            verifiserKanModifisere();
            kladd.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medBesteberegningPrÅr(Beløp besteberegningPrÅr) {
            verifiserKanModifisere();
            kladd.grunnlagPrÅr.setBesteberegningPrÅr(besteberegningPrÅr);
            oppdaterPeriodebrutto();
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                kladd.andelsnr = andelsnr;
            }
            return this;
        }

        public Builder nyttAndelsnr(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            }
            return this;
        }

        public Builder medKilde(AndelKilde kilde) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                kladd.kilde = kilde;
            }
            return this;
        }

        public BGAndelArbeidsforholdDto.Builder getBgAndelArbeidsforholdDtoBuilder() {
            return BGAndelArbeidsforholdDto.builder(kladd.bgAndelArbeidsforhold);
        }

        public Optional<BGAndelArbeidsforholdDto.Builder> oppdaterArbeidsforholdHvisFinnes() {
            return kladd.bgAndelArbeidsforhold != null ?
                    Optional.of(BGAndelArbeidsforholdDto.Builder.oppdater(Optional.of(kladd.bgAndelArbeidsforhold))) : Optional.empty();
        }

        public Builder medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder) {
            verifiserKanModifisere();
            kladd.bgAndelArbeidsforhold = bgAndelArbeidsforholdBuilder.build(kladd);
            return this;
        }

        public Builder medOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
            return this;
        }

        public BeregningsgrunnlagPrStatusOgAndelDto build() {
            if (this.kladd.andelsnr == null) {
                throw new IllegalStateException("Må sette andelsnr for å bygge uten referanse til periode");
            }
            if (built) {
                return kladd;
            }
            built = true;
            return kladd;
        }


        public BeregningsgrunnlagPrStatusOgAndelDto build(BeregningsgrunnlagPeriodeDto periode) {
            if (built) {
                return kladd;
            }
            kladd.beregningsgrunnlagPeriode = periode;
            verifyStateForBuild();
            if (kladd.andelsnr == null) {
                // TODO (OleSandbu): Ikke mod input!
                finnOgSettAndelsnr(periode);
            }
            // TODO (OleSandbu): Ikke mod input!
            periode.addBeregningsgrunnlagPrStatusOgAndel(kladd);
            periode.updateBruttoPrÅr();
            verifiserAndelsnr();
            built = true;
            return kladd;
        }

        private void finnOgSettAndelsnr(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            verifiserKanModifisere();
            Long forrigeAndelsnr = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .mapToLong(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr)
                    .max()
                    .orElse(0L);
            kladd.andelsnr = forrigeAndelsnr + 1L;
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            kladd.beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr)
                    .forEach(andelsnr -> {
                        if (andelsnrIBruk.contains(andelsnr)) {
                            throw new IllegalStateException("Utviklerfeil: Kan ikke bygge andel. Andelsnr eksisterer allerede på en annen andel i samme bgPeriode.");
                        }
                        andelsnrIBruk.add(andelsnr);
                    });
        }

        private void verifiserKanModifisere() {
            if (built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kladd.beregningsgrunnlagPeriode, "beregningsgrunnlagPeriode");
            Objects.requireNonNull(kladd.aktivitetStatus, "aktivitetStatus");
            if (AktivitetStatus.ARBEIDSTAKER.equals(kladd.getAktivitetStatus())
                    && OpptjeningAktivitetType.ARBEID.equals(kladd.getArbeidsforholdType())) {
                Objects.requireNonNull(kladd.bgAndelArbeidsforhold, "bgAndelArbeidsforhold");
            }
        }

    }
}
