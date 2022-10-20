package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;

public class FinnRapporterteInntekterForNæring implements FinnRapporterteInntekter {

	@Override
	public Optional<Periodeinntekt> finnRapportertInntekt(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getInntektsgrunnlag().getSistePeriodeinntektMedTypeSøknad();
	}
}
