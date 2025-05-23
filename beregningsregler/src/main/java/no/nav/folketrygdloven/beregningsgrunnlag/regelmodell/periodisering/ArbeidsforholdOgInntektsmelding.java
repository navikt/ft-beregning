package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;


import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;

public class ArbeidsforholdOgInntektsmelding {
    private Arbeidsforhold arbeidsforhold;
    private List<Refusjonskrav> refusjoner = Collections.emptyList();
    private List<Refusjonskrav> gyldigeRefusjonskrav = Collections.emptyList();
	private List<NaturalYtelse> naturalYtelser = Collections.emptyList();
    private Periode ansettelsesperiode;
    private LocalDate startdatoPermisjon;
    private Long andelsnr;
    private RefusjonskravFrist refusjonskravFrist;

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<Refusjonskrav> getRefusjoner() {
        return refusjoner;
    }

    public List<Refusjonskrav> getGyldigeRefusjonskrav() {
        return gyldigeRefusjonskrav;
    }

    public void setGyldigeRefusjonskrav(List<Refusjonskrav> gyldigeRefusjonskrav) {
        this.gyldigeRefusjonskrav = gyldigeRefusjonskrav;
    }

    public AktivitetStatusV2 getAktivitetStatus() {
        return AktivitetStatusV2.AT;
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

    public Optional<RefusjonskravFrist> getRefusjonskravFrist() {
        return Optional.ofNullable(refusjonskravFrist);
    }

	public boolean erNyAktivitet() {
		return andelsnr == null;
    }

	public boolean slutterFørSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return ansettelsesperiode.getTom().isBefore(skjæringstidspunkt.minusDays(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArbeidsforholdOgInntektsmelding)) return false;
	    var that = (ArbeidsforholdOgInntektsmelding) o;
        return Objects.equals(arbeidsforhold, that.arbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
        return new Builder(arbeidsforholdOgInntektsmelding);
    }

    public static class Builder {
        private final ArbeidsforholdOgInntektsmelding kladd;

        private Builder() {
            kladd = new ArbeidsforholdOgInntektsmelding();
        }

        private Builder(ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
            kladd = arbeidsforholdOgInntektsmelding;
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

        public Builder medRefusjonskravFrist(RefusjonskravFrist refusjonskravFrist) {
            kladd.refusjonskravFrist = refusjonskravFrist;
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
            ", naturalYtelser=" + naturalYtelser +
            ", ansettelsesperiode=" + ansettelsesperiode +
            ", startdatoPermisjon=" + startdatoPermisjon +
            ", andelsnr=" + andelsnr +
            '}';
    }
}
