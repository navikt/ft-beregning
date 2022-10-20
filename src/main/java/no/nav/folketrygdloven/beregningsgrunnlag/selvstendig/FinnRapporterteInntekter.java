package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.inaktiv.FinnRapporterteInntekterForInaktiv;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;

public interface FinnRapporterteInntekter {

	static FinnRapporterteInntekter finnImplementasjonForStatus(AktivitetStatus aktivitetStatus) {
		if (aktivitetStatus.equals(AktivitetStatus.BA)) {
			return new FinnRapporterteInntekterForInaktiv();
		} else if (aktivitetStatus.erSelvstendigNæringsdrivende()) {
			return new FinnRapporterteInntekterForNæring();
		}
		throw new IllegalStateException(aktivitetStatus + " har ingen implementasjon for FinnRapporterteInntekter");
	}

	Optional<Periodeinntekt> finnRapportertInntekt(BeregningsgrunnlagPeriode grunnlag);

}
