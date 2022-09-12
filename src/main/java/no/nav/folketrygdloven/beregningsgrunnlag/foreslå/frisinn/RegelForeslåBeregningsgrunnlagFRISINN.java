package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlagFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ-FRISINN";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode regelmodell) {
		super();
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
	    if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
		    return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
	    }

	    Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
	    var speclist = regelmodell.getAktivitetStatuser().stream()
			    .map(AktivitetStatusMedHjemmel::getAktivitetStatus)
			    .map(as -> RegelForeslåBeregningsgrunnlagFRISINN.velgSpecification(regelmodell, as))
			    .collect(Collectors.toList());
	    Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
			    rs.beregningsRegel("FRISINN pr status", "Fastsett beregningsgrunnlag pr status", speclist, new Beregnet());

        return foreslåBeregningsgrunnlag;
    }

	private static Specification<BeregningsgrunnlagPeriode> velgSpecification(BeregningsgrunnlagPeriode regelmodell, AktivitetStatus aktivitetStatus) {
		return switch (aktivitetStatus) {
			case ATFL -> new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification();
			case SN -> new RegelBeregningsgrunnlagSNFRISINN().getSpecification();
			case ATFL_SN -> new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(regelmodell).getSpecification();
			case AAP, DP -> new ForeslåBeregningsgrunnlagDPellerAAPFRISINN();
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification();
		};
	}
}
