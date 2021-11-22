package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class AndelGraderingImpl implements AndelGradering {
    private AktivitetStatusV2 aktivitetStatus;
    private List<Gradering> graderinger = new ArrayList<>();
	private Arbeidsforhold arbeidsforhold;
    private LocalDateTimeline<Boolean> nyAktivitetTidslinje;

    private AndelGraderingImpl() {
    }

    @Override
    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return graderinger;
    }

	@Override
	public boolean erNyAktivitetPåDato(LocalDate dato) {
		LocalDateSegment<Boolean> segment = nyAktivitetTidslinje.getSegment(new LocalDateInterval(dato, dato));
		return segment != null && segment.getValue();
	}

    @Override
    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelGraderingImpl kladd;

        private Builder() {
            kladd = new AndelGraderingImpl();
        }

	    public Builder medEksisterendeAktivitetFraDato(LocalDate fom) {
		    kladd.nyAktivitetTidslinje = new LocalDateTimeline<Boolean>(fom, TIDENES_ENDE, false);
		    return this;
	    }

	    public Builder medNyAktivitetFraDato(LocalDate fom) {
		    kladd.nyAktivitetTidslinje = new LocalDateTimeline<Boolean>(fom, TIDENES_ENDE, true);
		    return this;
	    }

	    public Builder medNyAktivitetTidslinje(LocalDateTimeline<Boolean> nyAktivitetTidslinje) {
		    kladd.nyAktivitetTidslinje = nyAktivitetTidslinje;
		    return this;
	    }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medGraderinger(List<Gradering> graderinger) {
            kladd.graderinger = graderinger;
            return this;
        }

	    public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public AndelGraderingImpl build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "AndelGraderingImpl{" +
            "aktivitetStatus=" + aktivitetStatus +
            ", graderinger=" + graderinger +
            ", arbeidsforhold=" + arbeidsforhold +
            '}';
    }
}
