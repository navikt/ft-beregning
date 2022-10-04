package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN;
import no.nav.folketrygdloven.beregningsgrunnlag.militær.RegelForeslåBeregningsgrunnlagMilitær;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.RegelForeslåBeregningsgrunnlagTY;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlag implements RuleService<BeregningsgrunnlagPeriode> {

	public static final String ID = "BG-FORESLÅ";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
		var speclist = regelmodell.getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.map(this::velgSpecification)
				.toList();
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
				rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", speclist, new Beregnet());

		// Fastsett alle BG per status
		return foreslåBeregningsgrunnlag;
	}

	private Specification<BeregningsgrunnlagPeriode> velgSpecification(AktivitetStatus aktivitetStatus) {
		if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
			return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
		}
		var sporingsproperty = new ServiceArgument("aktivitetStatus", aktivitetStatus);
		if (aktivitetStatus.erAAPellerDP()) {
			return new RegelFastsettBeregningsgrunnlagDPellerAAP().getSpecification().medEvaluationProperty(sporingsproperty);
		}

		return switch (aktivitetStatus) {
			case SN -> new RegelBeregningsgrunnlagSN().getSpecification().medEvaluationProperty(sporingsproperty);
			case ATFL -> new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case ATFL_SN -> new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case KUN_YTELSE -> new RegelForeslåBeregningsgrunnlagTY(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case MS -> new RegelForeslåBeregningsgrunnlagMilitær().getSpecification().medEvaluationProperty(sporingsproperty);
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
		};
	}
}
