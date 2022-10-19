package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalKjøreFortsettForeslå.ID)
public class SkalKjøreFortsettForeslå extends LeafSpecification<BeregningsgrunnlagPeriode> {

	/**
	 */
	static final String ID = "FP_BR 2.21";
	static final String BESKRIVELSE = "Skal fortsett beregningsgrunnlag kjøres?";

	public SkalKjøreFortsettForeslå() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		var beregnesSomMilitær = grunnlag.getBeregningsgrunnlag().getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.anyMatch(AktivitetStatus::erMilitær);
		var beregnesSomNæringsdrivende = grunnlag.getBeregningsgrunnlag().getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.anyMatch(a -> a.erSelvstendigNæringsdrivende() || a.equals(AktivitetStatus.MIDL_INAKTIV));
		return beregnesSomNæringsdrivende || beregnesSomMilitær
				? ja()
				: nei();
	}
}
