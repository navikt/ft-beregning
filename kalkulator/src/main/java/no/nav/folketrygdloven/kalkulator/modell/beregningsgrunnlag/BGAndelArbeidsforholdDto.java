package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;


public class BGAndelArbeidsforholdDto {

    @DiffIgnore
    private BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel;
    @SjekkVedKopiering
    private Arbeidsgiver arbeidsgiver;
    @SjekkVedKopiering
    private InternArbeidsforholdRefDto arbeidsforholdRef = InternArbeidsforholdRefDto.nullRef();
    @SjekkVedKopiering
    private Refusjon refusjon;
    @SjekkVedKopiering
    private Beløp naturalytelseBortfaltPrÅr;
    @SjekkVedKopiering
    private Beløp naturalytelseTilkommetPrÅr;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;

    private BGAndelArbeidsforholdDto() {
    }

    public BGAndelArbeidsforholdDto(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold) {
        this.arbeidsgiver = eksisterendeBGAndelArbeidsforhold.arbeidsgiver;
        this.arbeidsforholdRef = eksisterendeBGAndelArbeidsforhold.arbeidsforholdRef;
        this.naturalytelseBortfaltPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseTilkommetPrÅr;
        this.arbeidsperiodeFom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeTom;
        this.refusjon = eksisterendeBGAndelArbeidsforhold.refusjon != null ? new Refusjon(eksisterendeBGAndelArbeidsforhold.refusjon) : null;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
    }

    public Optional<Refusjon> getRefusjon() {
        return Optional.ofNullable(refusjon);
    }

    public Beløp getRefusjonskravPrÅr() {
        return refusjon != null ? refusjon.getRefusjonskravPrÅr() : null;
    }

    public Optional<Beløp> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<Beløp> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public Optional<LocalDate> getArbeidsperiodeTom() {
        return Optional.ofNullable(arbeidsperiodeTom);
    }

    public Intervall getArbeidsperiode() {
        if (arbeidsperiodeTom == null) {
            return Intervall.fraOgMed(arbeidsperiodeFom);
        }
        return Intervall.fraOgMedTilOgMed(arbeidsperiodeFom, arbeidsperiodeTom);
    }

    public String getArbeidsforholdOrgnr() {
        return getArbeidsgiver().getOrgnr();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return refusjon != null ? refusjon.getHjemmelForRefusjonskravfrist() : null;
    }

    public Beløp getSaksbehandletRefusjonPrÅr() {
        return refusjon != null ? refusjon.getSaksbehandletRefusjonPrÅr() : null;
    }

    public Beløp getFordeltRefusjonPrÅr() {
        return refusjon != null ? refusjon.getFordeltRefusjonPrÅr() : null;
    }

    /**
     * Refusjonskrav settes på forskjellige steder i beregning dersom avklaringsbehov oppstår.
     * Først settes refusjonskravPrÅr, deretter saksbehandletRefusjonPrÅr og til slutt fordeltRefusjonPrÅr.
     * Det er det sist avklarte beløpet som til en hver tid skal være gjeldende.
     *
     * @return returnerer det refusjonskravet som skal være gjeldende
     */
    public Beløp getGjeldendeRefusjonPrÅr() {
        return refusjon != null ? refusjon.getGjeldendeRefusjonPrÅr() : null;
    }

    /**
     * Returnerer refusjonskrav fra inntektsmelding om fristvilkåret er godkjent
     *
     * @return Innvilget refusjonskrav
     */
    public Beløp getInnvilgetRefusjonskravPrÅr() {
        return refusjon != null ? refusjon.getInnvilgetRefusjonskravPrÅr() : null;
    }


    private void medRefusjonskravPrÅr(Beløp refusjonskravPrÅr, Hjemmel hjemmel, Utfall refusjonskravFristUtfall) {
        if (refusjonskravPrÅr == null) {
            return;
        }
        if (this.refusjon == null) {
            this.refusjon = Refusjon.medRefusjonskravPrÅr(refusjonskravPrÅr, hjemmel, refusjonskravFristUtfall);
        } else {
            this.refusjon.setRefusjonskravPrÅr(refusjonskravPrÅr);
        }
    }

    private void medSaksbehandletRefusjonPrÅr(Beløp saksbehandletRefusjonPrÅr) {
        if (saksbehandletRefusjonPrÅr == null) {
            return;
        }
        if (refusjon == null) {
            refusjon = Refusjon.medSaksbehandletRefusjonPrÅr(saksbehandletRefusjonPrÅr);
        } else {
            refusjon.setSaksbehandletRefusjonPrÅr(saksbehandletRefusjonPrÅr);
        }
    }

    private void medFordeltRefusjonPrÅr(Beløp fordeltRefusjonPrÅr) {
        if (fordeltRefusjonPrÅr == null) {
            return;
        }
        if (refusjon == null) {
            refusjon = Refusjon.medFordeltRefusjonPrÅr(fordeltRefusjonPrÅr);
        } else {
            refusjon.setFordeltRefusjonPrÅr(fordeltRefusjonPrÅr);
        }
    }

    private void medManueltFordeltRefusjon(Beløp manueltFordeltRefusjonPrÅr) {
        if (manueltFordeltRefusjonPrÅr == null) {
            return;
        }
        if (refusjon == null) {
            refusjon = Refusjon.medManueltFordeltRefusjonPrÅr(manueltFordeltRefusjonPrÅr);
        } else {
            refusjon.setManueltFordeltRefusjonPrÅr(manueltFordeltRefusjonPrÅr);
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BGAndelArbeidsforholdDto)) {
            return false;
        }
        BGAndelArbeidsforholdDto other = (BGAndelArbeidsforholdDto) obj;
        return Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver())
                && Objects.equals(this.arbeidsforholdRef, other.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "arbeidsgiver=" + arbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "refusjon=" + refusjon + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsperiodeFom=" + arbeidsperiodeFom //$NON-NLS-1$
                + "arbeidsperiodeTom=" + arbeidsperiodeTom //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
        return bgAndelArbeidsforhold == null ? new Builder() : new Builder(bgAndelArbeidsforhold);
    }

    public static class Builder {
        private BGAndelArbeidsforholdDto bgAndelArbeidsforhold;
        private boolean erOppdatering;

        private Builder() {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdDto();
        }

        private Builder(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold) {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdDto(eksisterendeBGAndelArbeidsforhold);
        }

        private Builder(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold, boolean erOppdatering) {
            bgAndelArbeidsforhold = eksisterendeBGAndelArbeidsforhold;
            this.erOppdatering = erOppdatering;
        }

        private static Builder oppdater(BGAndelArbeidsforholdDto oppdatere) {
            return new Builder(oppdatere, true);
        }

        static Builder ny() {
            return new Builder(new BGAndelArbeidsforholdDto(), false);
        }

        public static Builder oppdater(Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforhold) {
            return bgAndelArbeidsforhold.map(Builder::oppdater).orElseGet(Builder::ny);
        }

        public static Builder kopier(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
            return new Builder(bgAndelArbeidsforhold);
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            bgAndelArbeidsforhold.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            return medArbeidsforholdRef(arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto.ref(arbeidsforholdRef));
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            bgAndelArbeidsforhold.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(Beløp naturalytelseBortfaltPrÅr) {
            bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(Beløp naturalytelseTilkommetPrÅr) {
            bgAndelArbeidsforhold.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medRefusjon(Refusjon refusjon) {
            bgAndelArbeidsforhold.refusjon = refusjon;
            return this;
        }

        public Builder medRefusjonskravPrÅr(Beløp refusjonskravPrÅr, Utfall refusjonskravFristUtfall) {
            return medRefusjonskravPrÅr(refusjonskravPrÅr, Hjemmel.F_22_13_6, refusjonskravFristUtfall);
        }

        public Builder medRefusjonskravPrÅr(Beløp refusjonskravPrÅr, Hjemmel hjemmel, Utfall refusjonskravFristUtfall) {
            bgAndelArbeidsforhold.medRefusjonskravPrÅr(refusjonskravPrÅr, hjemmel, refusjonskravFristUtfall);
            return this;
        }

        public Builder medSaksbehandletRefusjonPrÅr(Beløp saksbehandletRefusjonPrÅr) {
            bgAndelArbeidsforhold.medSaksbehandletRefusjonPrÅr(saksbehandletRefusjonPrÅr);
            return this;
        }

        public Builder medFordeltRefusjonPrÅr(Beløp fordeltRefusjonPrÅr) {
            bgAndelArbeidsforhold.medFordeltRefusjonPrÅr(fordeltRefusjonPrÅr);
            return this;
        }

        public Builder medManueltFordeltRefusjonPrÅr(Beløp manueltFordeltRefusjonPrÅr) {
            bgAndelArbeidsforhold.medManueltFordeltRefusjon(manueltFordeltRefusjonPrÅr);
            return this;
        }


        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            bgAndelArbeidsforhold.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            bgAndelArbeidsforhold.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        public Builder medHjemmel(Hjemmel hjemmel) {
            if (hjemmel == null || hjemmel == Hjemmel.UDEFINERT) {
                return this;
            }
            if (bgAndelArbeidsforhold.refusjon == null) {
                bgAndelArbeidsforhold.refusjon = Refusjon.medRefusjonskravPrÅr(null, hjemmel, null);
            } else {
                bgAndelArbeidsforhold.refusjon.setHjemmelForRefusjonskravfrist(hjemmel);
            }
            return this;
        }

        BGAndelArbeidsforholdDto build(BeregningsgrunnlagPrStatusOgAndelDto andel) {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            bgAndelArbeidsforhold.beregningsgrunnlagPrStatusOgAndel = andel;
            return bgAndelArbeidsforhold;
        }

        public BGAndelArbeidsforholdDto build() {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            return bgAndelArbeidsforhold;
        }
    }
}
