package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlagFRISINN implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ-FRISINN";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode regelmodell) {
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
				rs.beregningsRegel("FRISINN pr status", "Fastsett beregningsgrunnlag pr status", speclist, new Beregnet());

		return foreslåBeregningsgrunnlag;
    }

	private Specification<BeregningsgrunnlagPeriode> velgSpecification(AktivitetStatus aktivitetStatus) {
		if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
			return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
		}
		var sporingsproperty = new ServiceArgument("aktivitetStatus", aktivitetStatus);
		return switch (aktivitetStatus) {
			case ATFL -> new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case SN -> new RegelBeregningsgrunnlagSNFRISINN().getSpecification().medEvaluationProperty(sporingsproperty);
			case ATFL_SN -> new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case AAP, DP -> new ForeslåBeregningsgrunnlagDPellerAAPFRISINN().medEvaluationProperty(sporingsproperty);
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
		};
	}
}
