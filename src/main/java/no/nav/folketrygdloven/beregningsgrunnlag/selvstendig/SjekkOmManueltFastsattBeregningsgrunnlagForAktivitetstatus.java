package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus.ID)
public class SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR 2.19";
	static final String BESKRIVELSE = "Har saksbehandler fastsatt beregningsgrunnlaget manuelt?";
	private final AktivitetStatus aktivitetStatus;

	public SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus(AktivitetStatus aktivitetStatus) {
		super(ID, BESKRIVELSE);
		this.aktivitetStatus = aktivitetStatus;
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		var beregningsgrunnlagPrStatus = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
		if (beregningsgrunnlagPrStatus == null) {
			return nei();
		}
		return Boolean.TRUE.equals(beregningsgrunnlagPrStatus.erFastsattAvSaksbehandler())
				? ja()
				: nei();
	}
}
