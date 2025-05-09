package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class ArbeidUnderAAPTjeneste {
	public static boolean harAndelForArbeidUnderAAP(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
		return beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().getFirst()
				.getBeregningsgrunnlagPrStatusOgAndelList().stream()
				.anyMatch(andel -> andel.getArbeidsforholdType().equals(OpptjeningAktivitetType.ARBEID_UNDER_AAP));
	}
}
