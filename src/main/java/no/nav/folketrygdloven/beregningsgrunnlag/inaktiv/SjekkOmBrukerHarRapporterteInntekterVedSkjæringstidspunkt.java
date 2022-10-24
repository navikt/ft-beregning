package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerHarRapporterteInntekterVedSkjæringstidspunkt.ID)
public class SjekkOmBrukerHarRapporterteInntekterVedSkjæringstidspunkt extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR 2.3";
	static final String BESKRIVELSE = "Har bruker rapporterte inntekter ved skjæringstidspunktet?";

	public SjekkOmBrukerHarRapporterteInntekterVedSkjæringstidspunkt() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		var harInnrapportertInntektVedStp = new FinnRapporterteInntekterForInaktiv().finnRapportertInntekt(grunnlag).isPresent();
		return harInnrapportertInntektVedStp ? ja() : nei();
	}
}
