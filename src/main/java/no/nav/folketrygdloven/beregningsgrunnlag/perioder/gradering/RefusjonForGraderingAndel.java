package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;

class RefusjonForGraderingAndel {

	static boolean harRefusjonPåDato(AndelGradering andelGradering, List<PeriodisertBruttoBeregningsgrunnlag> periodisertBg, LocalDate graderingFom) {
		return periodisertBg.stream()
				.filter(p -> p.getPeriode().inneholder(graderingFom))
				.flatMap(p -> p.getBruttoBeregningsgrunnlag().stream())
				.filter(b -> b.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()) && matcherArbeidsforhold(andelGradering, b.getArbeidsforhold()))
				.map(BruttoBeregningsgrunnlag::getRefusjonPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(BigDecimal.ZERO) > 0;
	}

	private static boolean matcherArbeidsforhold(AndelGradering andelGradering, Optional<Arbeidsforhold> arbeidsforhold) {
		if (andelGradering.getArbeidsforhold() != null && arbeidsforhold.isPresent()) {
			return andelGradering.getArbeidsforhold().getArbeidsgiverId().equals(arbeidsforhold.get().getArbeidsgiverId()) &&
					matcherArbeidsforholdId(andelGradering.getArbeidsforhold().getArbeidsforholdId(), arbeidsforhold.get().getArbeidsforholdId());
		}
		return andelGradering.getArbeidsforhold() == null && arbeidsforhold.isEmpty();
	}

	private static boolean matcherArbeidsforholdId(String arbeidsforholdId1, String arbeidsforholdId2) {
		return arbeidsforholdId1 == null || arbeidsforholdId2 == null || arbeidsforholdId1.equals(arbeidsforholdId2);
	}

}
