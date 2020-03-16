package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;

public class ArbeidsforholdOgInntektsmelding implements AndelGradering {
    private Arbeidsforhold arbeidsforhold;
    private List<Refusjonskrav> refusjoner = Collections.emptyList();
    private List<Refusjonskrav> gyldigeRefusjonskrav = Collections.emptyList();
    private List<Gradering> graderinger = Collections.emptyList();
    private List<NaturalYtelse> naturalYtelser = Collections.emptyList();
    private Periode ansettelsesperiode;
    private LocalDate startdatoPermisjon;
    private Long andelsnr;
    private LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon;
    private LocalDate overstyrtRefusjonsFrist;

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<Refusjonskrav> getRefusjoner() {
        return refusjoner;
    }

    @Override
    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return gyldigeRefusjonskrav;
    }

    public void setGyldigeRefusjonskrav(List<Refusjonskrav> gyldigeRefusjonskrav) {
        this.gyldigeRefusjonskrav = gyldigeRefusjonskrav;
    }

    @Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return AktivitetStatusV2.AT;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

    public List<NaturalYtelse> getNaturalYtelser() {
        return naturalYtelser;
    }

    public Periode getAnsettelsesperiode() {
        return ansettelsesperiode;
    }

    public LocalDate getStartdatoPermisjon() {
        return startdatoPermisjon;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public LocalDate getInnsendingsdatoFørsteInntektsmeldingMedRefusjon() {
        return innsendingsdatoFørsteInntektsmeldingMedRefusjon;
    }

    public Optional<LocalDate> getOverstyrtRefusjonsFrist() {
        return Optional.ofNullable(overstyrtRefusjonsFrist);
    }

    @Override
    public boolean erNyAktivitet() { return andelsnr == null; }

    public boolean slutterFørSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return ansettelsesperiode.getTom().isBefore(skjæringstidspunkt.minusDays(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArbeidsforholdOgInntektsmelding)) return false;
        ArbeidsforholdOgInntektsmelding that = (ArbeidsforholdOgInntektsmelding) o;
        return Objects.equals(arbeidsforhold, that.arbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ArbeidsforholdOgInntektsmelding kladd;

        private Builder() {
            kladd = new ArbeidsforholdOgInntektsmelding();
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medRefusjonskrav(List<Refusjonskrav> refusjoner) {
            kladd.refusjoner = refusjoner;
            return this;
        }

        public Builder medGyldigeRefusjonskrav(List<Refusjonskrav> gyldigeRefusjonskrav) {
            kladd.gyldigeRefusjonskrav = gyldigeRefusjonskrav;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

        public Builder medNaturalytelser(List<NaturalYtelse> naturalYtelser) {
            kladd.naturalYtelser = naturalYtelser;
            return this;
        }

        public Builder medAnsettelsesperiode(Periode ansettelsesperiode) {
            kladd.ansettelsesperiode = ansettelsesperiode;
            return this;
        }

        public Builder medStartdatoPermisjon(LocalDate startdatoPermisjon) {
            kladd.startdatoPermisjon = startdatoPermisjon;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon) {
            kladd.innsendingsdatoFørsteInntektsmeldingMedRefusjon = innsendingsdatoFørsteInntektsmeldingMedRefusjon;
            return this;
        }

        public Builder medOverstyrtRefusjonsFrist(LocalDate overstyrtRefusjonsFrist) {
            kladd.overstyrtRefusjonsFrist = overstyrtRefusjonsFrist;
            return this;
        }

        public ArbeidsforholdOgInntektsmelding build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "ArbeidsforholdOgInntektsmelding{" +
            "arbeidsforhold=" + arbeidsforhold +
            ", refusjoner=" + refusjoner +
            ", gyldigeRefusjonskrav=" + gyldigeRefusjonskrav +
            ", graderinger=" + graderinger +
            ", naturalYtelser=" + naturalYtelser +
            ", ansettelsesperiode=" + ansettelsesperiode +
            ", startdatoPermisjon=" + startdatoPermisjon +
            ", andelsnr=" + andelsnr +
            ", innsendingsdatoFørsteInntektsmeldingMedRefusjon=" + innsendingsdatoFørsteInntektsmeldingMedRefusjon +
            ", overstyrtRefusjonsFrist=" + overstyrtRefusjonsFrist +
            '}';
    }
}
