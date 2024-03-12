package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class PeriodeModellRefusjon {
    private List<ArbeidsforholdOgInntektsmelding> arbeidsforholdOgInntektsmeldinger = Collections.emptyList();
    private Map<Arbeidsgiver, LocalDateTimeline<Utfall>> utfalltidslinjePrArbeidsgiver;
    private LocalDate skjæringstidspunkt;
    private List<SplittetPeriode> eksisterendePerioder = new ArrayList<>();

    public List<ArbeidsforholdOgInntektsmelding> getArbeidsforholdOgInntektsmeldinger() {
        return arbeidsforholdOgInntektsmeldinger;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<SplittetPeriode> getEksisterendePerioder() {
        return eksisterendePerioder;
    }

	public Map<Arbeidsgiver, LocalDateTimeline<Utfall>> getUtfalltidslinjePrArbeidsgiver() {
		return utfalltidslinjePrArbeidsgiver;
	}

	public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodeModellRefusjon kladd;

        public Builder() {
            kladd = new PeriodeModellRefusjon();
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medInntektsmeldinger(List<ArbeidsforholdOgInntektsmelding> inntektsmeldinger) {
            kladd.arbeidsforholdOgInntektsmeldinger = inntektsmeldinger;
            return this;
        }


	    public Builder medUtfallPrArbeidsgiver(Map<Arbeidsgiver, LocalDateTimeline<Utfall>> utfalltidslinjePrArbeidsgiver) {
		    kladd.utfalltidslinjePrArbeidsgiver = utfalltidslinjePrArbeidsgiver;
		    return this;
	    }

	    public Builder medEksisterendePerioder(List<SplittetPeriode> eksisterendePerioder) {
		    kladd.eksisterendePerioder = eksisterendePerioder;
		    return this;
	    }

        public PeriodeModellRefusjon build() {
            return kladd;
        }
    }
}
