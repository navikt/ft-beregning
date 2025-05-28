package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.FastsettIkkeSøktForTil0;
import no.nav.folketrygdloven.beregningsgrunnlag.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFullføreBeregningsgrunnlagFRISINN implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        var fastsettForSN = rs.beregningsRegel(FastsettForSN.ID, FastsettForSN.BESKRIVELSE,
                new FastsettForSN(), new ReduserBeregningsgrunnlag());

        var fastsettForFrilans = rs.beregningsRegel(FastsettForFrilans.ID, FastsettForFrilans.BESKRIVELSE,
                new FastsettForFrilans(), fastsettForSN);

        var fastsettIkkeSøktForTilNull = rs.beregningsRegel(FastsettIkkeSøktForTil0.ID, FastsettIkkeSøktForTil0.BESKRIVELSE,
                new FastsettIkkeSøktForTil0(), fastsettForFrilans);

        return fastsettIkkeSøktForTilNull;
    }
}
