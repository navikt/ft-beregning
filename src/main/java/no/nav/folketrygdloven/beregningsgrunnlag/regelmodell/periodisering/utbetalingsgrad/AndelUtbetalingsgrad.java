package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class AndelUtbetalingsgrad {
    private AktivitetStatusV2 aktivitetStatus;
    private List<Utbetalingsgrad> utbetalingsgrader = new ArrayList<>();
	private Arbeidsforhold arbeidsforhold;
    private LocalDateTimeline<Boolean> nyAktivitetTidslinje;

    private AndelUtbetalingsgrad() {
    }

    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    public List<Utbetalingsgrad> getUbetalingsgrader() {
        return utbetalingsgrader;
    }

	public boolean erNyAktivitetPåDato(LocalDate dato) {
		LocalDateSegment<Boolean> segment = nyAktivitetTidslinje.getSegment(new LocalDateInterval(dato, dato));
		return segment != null && segment.getValue();
	}

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AndelUtbetalingsgrad kladd;

        private Builder() {
            kladd = new AndelUtbetalingsgrad();
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

        public Builder medUtbetalingsgrader(List<Utbetalingsgrad> utbetalingsgrader) {
            kladd.utbetalingsgrader = utbetalingsgrader;
            return this;
        }

	    public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public AndelUtbetalingsgrad build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "AndelGraderingImpl{" +
            "aktivitetStatus=" + aktivitetStatus +
            ", graderinger=" + utbetalingsgrader +
            ", arbeidsforhold=" + arbeidsforhold +
            '}';
    }
}
