package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.FastsettIkkeSøktForTil0;
import no.nav.folketrygdloven.beregningsgrunnlag.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFullføreBeregningsgrunnlagFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

    BeregningsgrunnlagPeriode regelmodell;

	public RegelFullføreBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode regelmodell) {
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
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> fastsettForSN = rs.beregningsRegel(FastsettForSN.ID, FastsettForSN.BESKRIVELSE,
                new FastsettForSN(), new ReduserBeregningsgrunnlag());

        Specification<BeregningsgrunnlagPeriode> fastsettForFrilans = rs.beregningsRegel(FastsettForFrilans.ID, FastsettForFrilans.BESKRIVELSE,
                new FastsettForFrilans(), fastsettForSN);

        Specification<BeregningsgrunnlagPeriode> fastsettIkkeSøktForTilNull = rs.beregningsRegel(FastsettIkkeSøktForTil0.ID, FastsettIkkeSøktForTil0.BESKRIVELSE,
                new FastsettIkkeSøktForTil0(), fastsettForFrilans);

        return fastsettIkkeSøktForTilNull;
    }
}
